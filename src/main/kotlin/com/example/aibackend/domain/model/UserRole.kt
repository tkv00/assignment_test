package com.example.aibackend.domain.model

enum class UserRole(
    val databaseValue: String,
) {
    MEMBER("member"),
    ADMIN("admin"),
    ;

    companion object {
        /**
         * [사용자 권한 변환]
         * 데이터베이스 권한 값을 사용자 권한 enum으로 변환
         *
         * @param databaseValue 데이터베이스에 저장된 사용자 권한 값
         * @return 데이터베이스 값과 일치하는 사용자 권한
         */
        fun fromDatabaseValue(databaseValue: String): UserRole =
            entries.firstOrNull { role -> role.databaseValue == databaseValue }
                ?: throw IllegalArgumentException("Unsupported user role: $databaseValue")
    }
}
