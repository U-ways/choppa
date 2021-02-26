package app.choppa.domain.account

import app.choppa.domain.account.provider.*
import app.choppa.exception.NoOAuth2ProviderFoundException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service

@Service
class AccountService(
    @Autowired private val accountRepository: AccountRepository,
) {

    companion object {
        val CONVERTERS = mapOf(
            "choppa" to ChoppaOAuth2Provider(),
            "github" to GithubOAuth2Provider(),
            "microsoft" to MicrosoftOAuth2Provider(),
            "google" to GoogleOAuth2Provider(),
            "okta" to OktaOAuth2Provider(),
        )
    }

    fun convert(authentication: Authentication): Account {
        return convert(
            (SecurityContextHolder.getContext().authentication as OAuth2AuthenticationToken).authorizedClientRegistrationId,
            SecurityContextHolder.getContext().authentication.principal as OAuth2User
        )
    }

    fun convert(provider: String, oauth2User: OAuth2User): Account {
        val converter = CONVERTERS[provider] ?: throw NoOAuth2ProviderFoundException(provider)
        val providerId = converter.uniqueIdentifier(oauth2User)
        val name = converter.name(oauth2User)

        return if (accountRepository.existsAccountByProviderAndProviderId(provider, providerId)) {
            val savedAccount = accountRepository.findAccountByProviderAndProviderId(provider, providerId)
            savedAccount.name = name
            savedAccount.profilePicture = converter.profilePicture(oauth2User)
            savedAccount
        } else {
            Account.createFirstLogin(provider, providerId, name)
        }
    }

    fun isFirstTimeLogin(account: Account): Boolean = !accountRepository
        .existsAccountByProviderAndProviderId(account.provider, account.providerId)

    fun createAccount(account: Account): Account {
        if (!isFirstTimeLogin(account)) {
            throw Exception("An account already exists for this provider and providerId.")
        }

        return accountRepository.save(account)
    }
}
