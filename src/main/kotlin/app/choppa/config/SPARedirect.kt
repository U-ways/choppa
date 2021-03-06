package app.choppa.config

import org.springframework.boot.autoconfigure.web.servlet.error.ErrorViewResolver
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.servlet.ModelAndView
import javax.servlet.http.HttpServletRequest

@Component
class SPARedirect : ErrorViewResolver {
    override fun resolveErrorView(
        request: HttpServletRequest?,
        status: HttpStatus?,
        model: MutableMap<String, Any>?,
    ): ModelAndView {
        return ModelAndView("forward:/index.html")
    }
}
