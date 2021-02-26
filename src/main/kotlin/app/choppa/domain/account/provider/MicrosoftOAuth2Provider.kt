package app.choppa.domain.account.provider

import org.springframework.security.oauth2.core.user.OAuth2User

class MicrosoftOAuth2Provider : OAuth2Provider {
    override fun uniqueIdentifier(oauth2User: OAuth2User): String = oauth2User.getAttribute<String>("sub")!!
}
