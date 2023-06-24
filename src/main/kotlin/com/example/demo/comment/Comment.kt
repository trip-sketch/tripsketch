// package com.example.yourprojectname.database.tables

// import org.jetbrains.exposed.dao.id.LongIdTable
// import org.jetbrains.exposed.sql.Column
// import org.jetbrains.exposed.sql.`java-time`.timestamp
// import org.jetbrains.exposed.sql.jodatime.datetime
// import org.jetbrains.exposed.sql.jodatime.text
// import org.postgresql.util.PGobject

// +------------+-----------+------+-----+-------------------+-----------------------------------------------+
// | Field      | Type      | Null | Key | Default           | Extra                                         |
// +------------+-----------+------+-----+-------------------+-----------------------------------------------+
// | id         | bigint    | NO   | PRI | NULL              | auto_increment                                |
// | user_id    | bigint    | NO   | MUL | NULL              |                                               |
// | trip_id    | bigint    | NO   | MUL | NULL              |                                               |
// | parent_id  | bigint    | YES  | MUL | NULL              |                                               |
// | content    | text      | YES  |     | NULL              |                                               |
// | created_at | timestamp | NO   |     | CURRENT_TIMESTAMP | DEFAULT_GENERATED                             |
// | updated_at | timestamp | NO   |     | CURRENT_TIMESTAMP | DEFAULT_GENERATED on update CURRENT_TIMESTAMP |
// | likes      | int       | NO   |     | 0                 |                                               |
// | liked_by   | json      | NO   |     | NULL              |                                               |
// +------------+-----------+------+-----+-------------------+-----------------------------------------------+

// object CommentTable : LongIdTable("comments") {
//     val userId: Column<Long> = long("user_id").references(UserTable.id)
//     val tripId: Column<Long> = long("trip_id").references(TripTable.id)
//     val parentId: Column<Long?> = long("parent_id").nullable().references(CommentTable.id)
//     val content: Column<String?> = text("content").nullable()
//     val createdAt: Column<java.time.LocalDateTime> = timestamp("created_at")
//         .default(java.time.LocalDateTime.now())
//     val updatedAt: Column<java.time.LocalDateTime> = timestamp("updated_at")
//         .default(java.time.LocalDateTime.now())
//     val likes: Column<Int> = integer("likes").default(0)
//     val likedBy: Column<PGobject> = customEnumeration(
//         "liked_by", "json", { value -> PGobject().also { it.type = "json"; it.value = value as String? } }, { it -> it?.value }
//     )
// }
