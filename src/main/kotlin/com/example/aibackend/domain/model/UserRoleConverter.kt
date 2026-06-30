package com.example.aibackend.domain.model

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter
class UserRoleConverter : AttributeConverter<UserRole, String> {
    /**
     * [사용자 권한 저장값 변환]
     * 사용자 권한 enum을 데이터베이스 저장값으로 변환
     *
     * @param attribute 사용자 권한 enum 또는 null
     * @return 데이터베이스에 저장할 사용자 권한 값 또는 null
     */
    override fun convertToDatabaseColumn(attribute: UserRole?): String? = attribute?.databaseValue

    /**
     * [사용자 권한 엔티티값 변환]
     * 데이터베이스 사용자 권한 값을 enum으로 변환
     *
     * @param dbData 데이터베이스에 저장된 사용자 권한 값 또는 null
     * @return 엔티티에서 사용할 사용자 권한 enum 또는 null
     */
    override fun convertToEntityAttribute(dbData: String?): UserRole? = dbData?.let(UserRole::fromDatabaseValue)
}
