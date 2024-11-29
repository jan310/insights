package jan.ondra.insights.business

import jan.ondra.insights.models.User
import jan.ondra.insights.persistence.UserRepository
import org.springframework.stereotype.Service

@Service
class UserService(private val userRepository: UserRepository) {

    fun createUser(user: User) {
        userRepository.createUser(user)
    }

    fun getUser(id: String): User {
        return userRepository.getUser(id)
    }

    fun updateUser(user: User) {
        userRepository.updateUser(user)
    }

    fun deleteUser(id: String) {
        userRepository.deleteUser(id)
    }

}
