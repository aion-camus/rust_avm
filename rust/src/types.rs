use super::codec::{NativeDecoder, NativeEncoder};

type Bytes = Vec<u8>;

pub const AVM_CREATE: u8 = 2;
pub const AVM_CALL: u8 = 3;

#[derive(Debug)]
pub struct TransactionContext {
    pub transaction_type: u8,
    pub address: Bytes,
    pub caller: Bytes,
    pub origin: Bytes,
    pub nonce: u64,
    pub value: Bytes,
    pub data: Bytes,
    pub energy_limit: u64,
    pub energy_price: u64,
    pub transaction_hash: Bytes,
    pub basic_cost: u32,
    pub transaction_timestamp: u64,
    pub block_timestamp: u64,
    pub block_number: u64,
    pub block_energy_limit: u64,
    pub block_coinbase: Bytes,
    pub block_previous_hash: Bytes,
    pub block_difficulty: Bytes,
    pub internal_call_depth: u32,
}

impl TransactionContext {

    // TODO: add a constructor which converts a Transaction and Block into this struct

    pub fn to_bytes(&self) -> Bytes {
        let mut enc = NativeEncoder::new();

        enc.encode_byte(self.transaction_type);
        enc.encode_bytes(&self.address);
        enc.encode_bytes(&self.caller);
        enc.encode_bytes(&self.origin);
        enc.encode_long(self.nonce);
        enc.encode_bytes(&self.value);
        enc.encode_bytes(&self.data);
        enc.encode_long(self.energy_limit);
        enc.encode_long(self.energy_price);
        enc.encode_bytes(&self.transaction_hash);
        enc.encode_int32(self.basic_cost);
        enc.encode_long(self.transaction_timestamp);
        enc.encode_long(self.block_timestamp);
        enc.encode_long(self.block_number);
        enc.encode_long(self.block_energy_limit);
        enc.encode_bytes(&self.block_coinbase);
        enc.encode_bytes(&self.block_previous_hash);
        enc.encode_bytes(&self.block_difficulty);
        enc.encode_int32(self.internal_call_depth);

        enc.to_bytes()
    }
}


#[derive(Debug)]
pub struct Log {
    pub address: Bytes,
    pub topics: Vec<Bytes>,
    pub data: Bytes,
}

#[derive(Debug)]
pub struct TransactionResult {
    pub code: u32,
    pub return_data: Bytes,
    pub energy_used: u64,
    pub storage_root_hash: u32,
    pub logs: Vec<Log>,
}

impl TransactionResult {
    pub fn new(bytes: Bytes) -> Result<TransactionResult, &'static str> {
        let mut decoder = NativeDecoder::new(&bytes);
        let code = decoder.decode_int()?;
        let return_data = decoder.decode_bytes()?;
        let energy_used = decoder.decode_long()?;
        let storage_root_hash = decoder.decode_int()?;

        let mut logs = Vec::<Log>::new();
        let num_of_logs = decoder.decode_int()?;
        for _i in 0..num_of_logs {
            let address = decoder.decode_bytes()?;
            let mut topics = Vec::<Bytes>::new();
            let num_of_topics = decoder.decode_int()?;
            for _j in 0..num_of_topics {
                topics.push(decoder.decode_bytes()?);
            };
            let data = decoder.decode_bytes()?;
            logs.push(Log {
                address,
                topics,
                data,
            });
        }

        Ok(TransactionResult {
            code,
            return_data,
            energy_used,
            storage_root_hash,
            logs,
        })
    }
}