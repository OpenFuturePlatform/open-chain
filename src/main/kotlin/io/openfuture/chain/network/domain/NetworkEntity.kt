package io.openfuture.chain.network.domain

import io.openfuture.chain.network.annotation.NoArgConstructor
import io.openfuture.chain.network.message.base.BaseMessage

//TODO: remove domain package after resolving merge conflict and migrating to messages
@NoArgConstructor
abstract class NetworkEntity : BaseMessage