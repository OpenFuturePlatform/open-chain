package io.openfuture.chain.core.model.entity.delegate

import org.hibernate.annotations.Immutable
import javax.persistence.*

@Entity
@Immutable
@Table(name = "delegates_view")
class ViewDelegate(

    @Id
    @Column(name = "id")
    var id: Int = 0,

    @Column(name = "public_key")
    var publicKey: String,

    @Column(name = "node_id")
    var nodeId: String,

    @Column(name = "address")
    var address: String,

    @Column(name = "host")
    var host: String,

    @Column(name = "port")
    var port: Int,

    @Column(name = "registration_date")
    var registrationDate: Long,

    @Column(name = "rating")
    var rating: Long,

    @Column(name = "votes_count")
    var votesCount: Long

)


