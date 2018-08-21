package io.openfuture.chain.network.message.core

import io.openfuture.chain.core.annotation.NoArgConstructor

@NoArgConstructor
class SyncBlockRequestMessage(
    hash: String
): HashMessage(hash)