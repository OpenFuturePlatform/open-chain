package io.openfuture.chain.network.message.core

import io.openfuture.chain.core.annotation.NoArgConstructor

@NoArgConstructor
class HashBlockResponseMessage(
    hash: String
): HashMessage(hash)
