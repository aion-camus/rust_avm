use callback::register_callbacks;
use codec::NativeDecoder;
use codec::NativeEncoder;
use rjni::{Classpath, JavaVM, Options, Type, Value, Version};
use rjni::ffi;
use std::fs;
use std::io::Error;
use std::path::Path;
use std::ptr;
use std::sync::atomic::AtomicPtr;
use std::sync::atomic::Ordering;
use std::thread;
use types::{TransactionContext, TransactionResult};

/// We keep a single JVM instance in the background, which will be shared
/// among multiple threads. Before invoking any JNI methods, the executing
/// thread needs to attach the thread to the JVM instance first and deattach
/// after finishing the interaction.
static mut JVM_SINGLETON: AtomicPtr<ffi::JavaVM> = AtomicPtr::new(ptr::null_mut());

/// Creates a JVM instance for the first time. This method is NOT thread
/// safe, and is intended for the "main" thread only.
pub fn launch_jvm() {
    unsafe {
        if ptr::eq(JVM_SINGLETON.load(Ordering::Relaxed), ptr::null_mut()) {
            let child = thread::spawn(move || {
                // prepare classpath
                let mut classpath = Classpath::new();
                classpath = add_jars(classpath, "../jni/build");
                classpath = add_jars(classpath, "../aion_vm/build/main");
                classpath = add_jars(classpath, "../aion_vm/lib");

                // prepare options
                let mut options = Options::new();
                options = options.version(Version::V16);
                options = options.classpath(classpath);

                // launch jvm
                let jvm = JavaVM::new(options)
                    .expect("Failed to launch a JVM instance");

                // register callbacks
                register_callbacks();

                // save the ffi::JavaVM instance
                JVM_SINGLETON.store(jvm.vm, Ordering::Relaxed);
            });

            child.join().expect("Couldn't join on the JVM launcher thread");
        }
    }
}

/// Add a jar file to the classpath, or all jars in the folder if the path
/// is a directory.
fn add_jars(cp: Classpath, path: &str) -> Classpath {
    let mut result = cp;

    if path.ends_with(".jar") {
        result = result.add(&Path::new(path));
    } else {
        let r = find_files(&Path::new(path), ".jar");
        match r {
            Ok(files) => {
                for file in files {
                    result = result.add(&Path::new(file.as_str()));
                }
            }
            Err(_) => {}
        }
    }

    result
}

/// Walk through the given path and return a list of files with the specified
/// extension.
fn find_files(path: &Path, ext: &str) -> Result<Vec<String>, Error> {
    let mut result: Vec<String> = vec![];

    if path.is_dir() {
        for entry in fs::read_dir(path)? {
            let path = entry?.path();
            let mut temp = find_files(&path, ext)?;
            result.append(&mut temp);
        }
    } else {
        let path_str = path.to_str();
        match path_str {
            Some(p) => {
                if p.ends_with(ext) {
                    result.push(String::from(p));
                }
            }
            None => {}
        }
    }

    return Ok(result);
}

/// Aion virtual machine
pub struct AVM {
    jvm: JavaVM,
}

impl Drop for AVM {
    fn drop(&mut self) {
        unsafe {
            ((**self.jvm.vm).DetachCurrentThread)(self.jvm.vm);
        }
    }
}

impl AVM {
    /// create a new AVM instance
    pub fn new() -> AVM {
        // launch a JVM if not done so far
        launch_jvm();

        // attach this thread to the JVM
        unsafe {
            let vm = JVM_SINGLETON.load(Ordering::Relaxed);
            let mut env = ptr::null_mut();

            ((**vm).AttachCurrentThread)(vm, &mut env, ptr::null_mut());

            AVM {
                jvm: JavaVM {
                    vm,
                    env,
                }
            }
        }
    }

    /// Executes a list of transactions
    pub fn execute(&self, transactions: &Vec<TransactionContext>) -> Result<Vec<TransactionResult>, &'static str> {
        // find the NativeTransactionExecutor class
        let class = self.jvm.class("org/aion/avm/jni/NativeTransactionExecutor")
            .expect("NativeTransactionExecutor is missing in the classpath");

        // the method name
        let name = "execute";

        // the method return type
        let return_type = Type::Object("[B");

        // the arguments
        let handle = 0i64;
        let arguments = [
            Value::Long(handle), // handle
            Value::Object(self.jvm.new_byte_array_with_data(&Self::encode_transaction_contexts(&transactions))
                .expect("Failed to create new byte array in JVM")
            ),
        ];

        // invoke the method
        let ret = class
            .call_static(name, &arguments, return_type)
            .expect("Failed to call the execute() method");

        if let Value::Object(obj) = ret {
            if obj.is_null() {
                Err("The execute() method failed")
            } else {
                let bytes = self.jvm.load_byte_array(&obj);
                Self::decode_transaction_results(&bytes)
            }
        } else {
            Err("The execute() method returns wrong data")
        }
    }

    /// Encodes transaction contexts into byte array
    fn encode_transaction_contexts(transactions: &Vec<TransactionContext>) -> Vec<u8> {
        let mut encoder = NativeEncoder::new();
        encoder.encode_int32(transactions.len() as u32);
        for i in 0..transactions.len() {
            encoder.encode_bytes(&transactions[i].to_bytes());
        }

        encoder.to_bytes()
    }

    /// Decodes transaction results from byte array
    fn decode_transaction_results(bytes: &Vec<u8>) -> Result<Vec<TransactionResult>, &'static str> {
        let mut results = Vec::<TransactionResult>::new();
        let mut decoder = NativeDecoder::new(bytes);
        let length = decoder.decode_int()?;
        for _i in 0..length {
            let result = decoder.decode_bytes()?;
            results.push(TransactionResult::new(result)?);
        }

        Ok(results)
    }
}

#[cfg(test)]
mod test {
    use avm::AVM;
    use codec::NativeEncoder;
    use std::fs::File;
    use std::io::Error;
    use std::io::Read;
    use std::path::PathBuf;
    use types::{TransactionContext, AVM_CREATE};

    #[test]
    fn test_avm_hello_world() {
        let avm = AVM::new();
        let transactions = prepare_transactions();
        println!("{:?}", transactions);
        let results = avm.execute(&transactions).unwrap();
        println!("{:?}", results);
    }

    fn prepare_transactions() -> Vec<TransactionContext> {
        let mut file = PathBuf::from(env!("CARGO_MANIFEST_DIR"));
        file.push("examples/com.example.helloworld.jar");
        let file_str = file.to_str().expect("Failed to locate the helloworld.jar");

        let tx1 = TransactionContext {
            transaction_type: AVM_CREATE,
            address: [1u8; 32].to_vec(),
            caller: [2u8; 32].to_vec(),
            origin: [3u8; 32].to_vec(),
            nonce: 0,
            value: Vec::new(),
            data: code_and_arguments(&read_file(file_str).expect("Failed to read the helloworld.jar"),
                                     Option::None),
            energy_limit: 1_000_000,
            energy_price: 1,
            transaction_hash: [4u8; 32].to_vec(),
            basic_cost: 200_000,
            transaction_timestamp: 2,
            block_timestamp: 3,
            block_number: 4,
            block_energy_limit: 5_000_000,
            block_coinbase: [4u8; 32].to_vec(),
            block_previous_hash: [5u8; 32].to_vec(),
            block_difficulty: Vec::new(),
            internal_call_depth: 0,
        };

        let mut ctxs = Vec::<TransactionContext>::new();
        ctxs.push(tx1);
        ctxs
    }

    fn code_and_arguments(code: &Vec<u8>, arguments: Option<&Vec<u8>>) -> Vec<u8> {
        let mut encoder = NativeEncoder::new();
        encoder.encode_bytes(code);
        match arguments {
            Some(arg) => encoder.encode_bytes(arg),
            None => {}
        }
        encoder.to_bytes()
    }

    fn read_file(path: &str) -> Result<Vec<u8>, Error> {
        let mut file = File::open(path)?;
        let mut buf = Vec::<u8>::new();
        file.read_to_end(&mut buf)?;
        Ok(buf)
    }
}
