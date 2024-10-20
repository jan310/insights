package jan.ondra.insights.business

import jan.ondra.insights.models.User
import jan.ondra.insights.persistence.UserRepository
import org.springframework.stereotype.Service

@Service
class UserService(private val userRepository: UserRepository) {

    fun create(user: User) {
        userRepository.create(user)
    }

    fun get(id: String): User {
        return userRepository.get(id)
    }

    fun update(user: User) {
        userRepository.update(user)
    }

    fun delete(id: String) {
        userRepository.delete(id)
    }

}
