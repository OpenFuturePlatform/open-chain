package io.openfuture.chain.network.component.time

import io.openfuture.chain.config.ServiceTests
import io.openfuture.chain.network.property.NodeProperties
import org.junit.Test
import org.mockito.BDDMockito.given
import org.mockito.Mock
import org.springframework.beans.factory.annotation.Autowired
import java.net.InetAddress

class ClockSynchronizerTest : ServiceTests() {

    @Autowired
    private lateinit var nodeProperties: NodeProperties

    @Mock
    private lateinit var sync: ClockSynchronizer

    @Test
    fun test() {
        val list = nodeProperties.ntpServers.map { InetAddress.getByName(it) }
        given(sync.requestTime(list[0])).willReturn(NtpResult(1, 2))
        given(sync.requestTime(list[1])).willReturn(NtpResult(15, 42))
        given(sync.requestTime(list[2])).willReturn(NtpResult(14, 12))
        given(sync.requestTime(list[3])).willReturn(NtpResult(40, 4242))
        given(sync.requestTime(list[4])).willReturn(NtpResult(6, 7))
    }
}