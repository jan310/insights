package jan.ondra.insights.business

import jan.ondra.insights.exception.EmailAlreadyExistsException
import jan.ondra.insights.exception.UserAlreadyRegisteredException
import jan.ondra.insights.exception.UserNotRegisteredException
import jan.ondra.insights.models.User
import jan.ondra.insights.persistence.UserRepository
import org.springframework.dao.DuplicateKeyException
import org.springframework.stereotype.Service

@Service
class UserService(private val userRepository: UserRepository) {

    fun create(user: User) {
        try {
            userRepository.create(user)
        } catch (duplicateKeyException: DuplicateKeyException) {
            duplicateKeyException.message?.let { message ->
                throw if (message.contains("duplicate key value violates unique constraint \"users_email_key\"")) {
                    EmailAlreadyExistsException(
                        clientInfo = "The email already exists",
                        serverLog = "A user tried to register using an email address that already exists",
                        cause = duplicateKeyException
                    )
                } else if (message.contains("duplicate key value violates unique constraint \"users_pkey\"")) {
                    UserAlreadyRegisteredException(
                        clientInfo = "Registration failed",
                        serverLog = "A registered user tried to register again",
                        cause = duplicateKeyException
                    )
                } else {
                    duplicateKeyException
                }
            } ?: throw duplicateKeyException
        }
    }

    fun update(user: User) {
        try {
            if (userRepository.update(user) == 0) {
                throw UserNotRegisteredException(
                    clientInfo = "User is not registered",
                    serverLog = "An unregistered user tried to change its email address",
                )
            }
        } catch (duplicateKeyException: DuplicateKeyException) {
            throw EmailAlreadyExistsException(
                clientInfo = "The email already exists",
                serverLog = "A user tried to change its email address to an address that already exists",
                cause = duplicateKeyException
            )
        }
    }

    fun delete(id: String) {
        if (userRepository.delete(id) == 0) {
            throw UserNotRegisteredException(
                clientInfo = "User is not registered",
                serverLog = "An unregistered user tried to delete its non-existent account",
            )
        }
    }

}
