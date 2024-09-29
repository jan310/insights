package jan.ondra.insights.business

import jan.ondra.insights.exception.EmailAlreadyExistsException
import jan.ondra.insights.exception.UserAlreadyRegisteredException
import jan.ondra.insights.exception.UserNotRegisteredException
import jan.ondra.insights.models.User
import jan.ondra.insights.persistence.UserRepository
import org.springframework.dao.DuplicateKeyException
import org.springframework.stereotype.Service

private const val DUPLICATE_EMAIL_VIOLATION = "duplicate key value violates unique constraint \"users_email_key\""
private const val DUPLICATE_ID_VIOLATION = "duplicate key value violates unique constraint \"users_pkey\""

@Service
class UserService(private val userRepository: UserRepository) {

    fun create(user: User) {
        try {
            userRepository.create(user)
        } catch (duplicateKeyException: DuplicateKeyException) {
            duplicateKeyException.message?.let { message ->
                throw if (message.contains(DUPLICATE_EMAIL_VIOLATION)) {
                    EmailAlreadyExistsException(cause = duplicateKeyException)
                } else if (message.contains(DUPLICATE_ID_VIOLATION)) {
                    UserAlreadyRegisteredException(cause = duplicateKeyException)
                } else {
                    duplicateKeyException
                }
            } ?: throw duplicateKeyException
        }
    }

    fun get(id: String): User {
        return userRepository.get(id) ?: throw UserNotRegisteredException()
    }

    fun update(user: User) {
        try {
            if (userRepository.update(user) == 0) {
                throw UserNotRegisteredException()
            }
        } catch (duplicateKeyException: DuplicateKeyException) {
            throw EmailAlreadyExistsException(cause = duplicateKeyException)
        }
    }

    fun delete(id: String) {
        if (userRepository.delete(id) == 0) {
            throw UserNotRegisteredException()
        }
    }

}
