# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/)
and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).

## [Unreleased]
### Added
- Delegate transaction type
- Add delegate based on Delegate transaction
- RPC: Response header info for all responses
- RPC: Add transactions
- RPC: Delegate info
- RPC: Page request filter
- Add / remove vote for delegate from wallet based on Vote transaction
- Calculate delegate rating based on wallet balance
- Fees to transactions
- New type transaction: RewardTransaction. New entity, repository, service
- Addition to block
- Genesis address to properties
- Constants fees and reward for transactions/block to properties
- Verify the reward transaction
- Version and timestamp fields to all packets
- Database: unconfirmed transactions tables
- Synchronizations data about blocks and transactions after node up

### Changed
- Serialization mechanism to support kotlin classes
- Structure of transactions

## [0.3.0] - 2018-07-13
### Added
- Address mixed-case checksum
- Address validation
- RPC requests: validateAddress
- Vote transaction type
- Voting algorithm mechanism
- Voting storing
- Delegate rating calculation
- Active delegates storing
- Wallet balance calculation
- Database: added Wallet entity
- RPC requests: getWalletBalance
- Sign data
- Verify signed data
- Define epoch duration in properties
- Get current epoch height
- Check if genesis block is needed
- Sharing peers between nodes
- Maintaining necessary connections count
- Connection establishing logic
- Scheduled check of connections
- Reconnection logic
- Generating private and public key pairs on app start and saving in a file if not existing
- Shuffle active delegates
- Transaction Merkle hash calculation
- Block production
- Block validation
- Block, transaction, signature conversion from protobuff to the business object and vice versa
- Accumulation of signatures from nodes
- Block approving
- Block applying by scheduling

### Changed
- Default account info is added to the doGenerateMaster end-point response
- doGenerateMaster end-point renamed to doRestore
- Block are divided by types: GenesisBlock and MainBlock

## [0.2.0] - 2018-06-29
### Added
- Seed phrase generator
- Seed phrase validator
- Generate a master key with seed phrase
- Key derivation algorithm
- Get derivation key of master key by seed phrase and derivation path
- Get a serialized public key value in base58 format from extended key
- Get serialized private key value in base58 format from extended key
- End-point: get a master key by seed phrase
- End-point: get derivation key with an address of master key by seed phrase and derivation path
- Extended private key serialization
- Extended public key serialization
- Export private key
- Import private key
- RPC requests: importKey
- RPC requests: importKey in WIF (Wallet Import Format)
- RPC requests: generateKeys

## [0.1.0] - 2018-06-18
### Added
- Setup Netty server for socket connection
- Setup Netty clients for socket connection
- Setup Netty server for HTTP
- Binary serialization communication protocol
- Binary deserialization communication protocol
- Time synchronization
- End-point chain stability check
- Heartbeat implementation
- Database structure
- Database: setup database connection
- Database: add Base entity
- Database: add Block entity
- Database: add Transaction entity
- Database: setup repository for Block entity
- Database: setup repository for Transaction entity
- Service: define base service interface for Block entity
- Service: define base service interface for Transaction entity
- Service: add base implementation Block entity service
- Service: add base implementation Transaction entity service
- RPC requests: getVersion
- RPC requests: getHardwareInfo
- RPC requests: getTimestamp

[Unreleased]: https://github.com/OpenFuturePlatform/open-chain/compare/master...sprint
[0.3.0]: https://github.com/OpenFuturePlatform/open-chain/compare/v0.2.0...v0.3.0
[0.2.0]: https://github.com/OpenFuturePlatform/open-chain/compare/v0.1.0...v0.2.0
[0.1.0]: https://github.com/OpenFuturePlatform/open-chain/compare/fea19b11de90c89689eff8d2187fd332ee566d19...v0.1.0