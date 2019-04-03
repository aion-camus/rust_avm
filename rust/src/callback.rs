extern crate hex;
extern crate libc;
extern crate num_bigint;

use core::fmt;
use core::slice;
use libc::c_void;
use num_bigint::BigUint;

#[derive(Debug)]
#[repr(C)]
pub struct avm_address {
    pub bytes: [u8; 32],
}

#[derive(Debug)]
#[repr(C)]
pub struct avm_value {
    pub bytes: [u8; 32],
}

#[derive(Debug)]
#[repr(C)]
pub struct avm_bytes {
    pub length: u32,
    pub pointer: *mut u8,
}

#[repr(C)]
pub struct avm_callbacks {
    pub create_account: extern fn(handle: *const c_void, address: *const avm_address),
    pub has_account_state: extern fn(handle: *const c_void, address: *const avm_address) -> u32,
    pub put_code: extern fn(handle: *const c_void, address: *const avm_address, code: *const avm_bytes),
    pub get_code: extern fn(handle: *const c_void, address: *const avm_address) -> avm_bytes,
    pub put_storage: extern fn(handle: *const c_void, address: *const avm_address, key: *const avm_bytes, value: *const avm_bytes),
    pub get_storage: extern fn(handle: *const c_void, address: *const avm_address, key: *const avm_bytes) -> avm_bytes,
    pub delete_account: extern fn(handle: *const c_void, address: *const avm_address),
    pub get_balance: extern fn(handle: *const c_void, address: *const avm_address) -> avm_value,
    pub increase_balance: extern fn(handle: *const c_void, address: *const avm_address, value: *const avm_value),
    pub decrease_balance: extern fn(handle: *const c_void, address: *const avm_address, value: *const avm_value),
    pub get_nonce: extern fn(handle: *const c_void, address: *const avm_address) -> u64,
    pub increment_nonce: extern fn(handle: *const c_void, address: *const avm_address),
}

#[repr(C)]
pub struct avm_rust_utils {
    pub new_contract_address: extern fn(address: *const avm_address, nonce: u64) -> avm_bytes
}

impl fmt::Display for avm_address {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        let s = hex::encode(self.bytes);
        write!(f, "0x{}", s)
    }
}

impl fmt::Display for avm_value {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        let s = BigUint::from_bytes_be(&self.bytes);
        write!(f, "{}", s)
    }
}

impl fmt::Display for avm_bytes {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        let bytes = unsafe { slice::from_raw_parts(self.pointer as *mut u8, self.length as usize) };
        let s = hex::encode(bytes);
        write!(f, "0x{}", s)
    }
}

#[link(name = "avmjni")]
extern "C" {
    pub static mut callbacks: avm_callbacks;
    pub static mut rust_utils: avm_rust_utils;

    #[allow(dead_code)]
    pub fn is_null(bytes: *const avm_bytes) -> bool;

    #[allow(dead_code)]
    pub fn new_fixed_bytes(length: u32) -> avm_bytes;

    #[allow(dead_code)]
    pub fn new_null_bytes() -> avm_bytes;

    #[allow(dead_code)]
    pub fn release_bytes(bytes: *mut avm_bytes);
}

#[no_mangle]
pub extern fn create_account(handle: *const c_void, address: *const avm_address) {
    unsafe {
        println!("Callback: create_account({:?}, {})", handle, *address);
    }
}

#[no_mangle]
pub extern fn has_account_state(handle: *const c_void, address: *const avm_address) -> u32 {
    unsafe {
        let result: u32 = 1;
        println!("Callback: has_account_state({:?}, {}) => {}", handle, *address, result);
        result
    }
}

#[no_mangle]
pub extern fn put_code(handle: *const c_void, address: *const avm_address, code: *const avm_bytes) {
    unsafe {
        println!("Callback: put_code({:?}, {}, {})", handle, *address, *code);
    }
}

#[no_mangle]
pub extern fn get_code(handle: *const c_void, address: *const avm_address) -> avm_bytes {
    unsafe {
        let code = new_null_bytes();
        println!("Callback: get_code({:?}, {}) => {}", handle, *address, code);
        code
    }
}

#[no_mangle]
pub extern fn put_storage(handle: *const c_void, address: *const avm_address,
                          key: *const avm_bytes, value: *const avm_bytes) {
    unsafe {
        println!("Callback: put_storage({:?}, {}, {}, {})", handle, *address, *key, *value);
    }
}

#[no_mangle]
pub extern fn get_storage(handle: *const c_void, address: *const avm_address,
                          key: *const avm_bytes) -> avm_bytes {
    unsafe {
        let value = new_null_bytes();
        println!("Callback: get_storage({:?}, {}, {}) => {}", handle, *address, *key, value);
        value
    }
}

#[no_mangle]
pub extern fn delete_account(handle: *const c_void, address: *const avm_address) {
    unsafe {
        println!("Callback: delete_account({:?}, {})", handle, *address);
    }
}

#[no_mangle]
pub extern fn get_balance(handle: *const c_void, address: *const avm_address) -> avm_value {
    unsafe {
        let balance = avm_value {
            bytes: [1u8; 32]
        };
        println!("Callback: get_balance({:?}, {}) => {}", handle, *address, balance);
        balance
    }
}

#[no_mangle]
pub extern fn increase_balance(handle: *const c_void, address: *const avm_address, value: *const avm_value) {
    unsafe {
        println!("Callback: increase_balance({:?}, {}, {})", handle, *address, *value);
    }
}

#[no_mangle]
pub extern fn decrease_balance(handle: *const c_void, address: *const avm_address, value: *const avm_value) {
    unsafe {
        println!("Callback: decrease_balance({:?}, {}, {})", handle, *address, *value);
    }
}

#[no_mangle]
pub extern fn get_nonce(handle: *const c_void, address: *const avm_address) -> u64 {
    unsafe {
        let nonce = 0;
        println!("Callback: get_nonce({:?}, {}) => {}", handle, *address, nonce);
        nonce
    }
}

#[no_mangle]
pub extern fn increment_nonce(handle: *const c_void, address: *const avm_address) {
    unsafe {
        println!("Callback: increment_nonce({:?}, {})", handle, *address);
    }
}

pub fn register_callbacks() {
    unsafe {
        callbacks.create_account = create_account;
        callbacks.has_account_state = has_account_state;
        callbacks.get_balance = get_balance;
        callbacks.put_code = put_code;
        callbacks.get_code = get_code;
        callbacks.put_storage = put_storage;
        callbacks.get_storage = get_storage;
        callbacks.delete_account = delete_account;
        callbacks.increase_balance = increase_balance;
        callbacks.decrease_balance = decrease_balance;
        callbacks.get_nonce = get_nonce;
        callbacks.increment_nonce = increment_nonce;
    }
}