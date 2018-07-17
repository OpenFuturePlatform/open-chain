package io.openfuture.chain.block

import io.openfuture.chain.entity.Delegate
import org.springframework.stereotype.Component


@Component
class TimeSlot {

    var roundStartTime: Long = 0L
    lateinit var producer: Delegate

}