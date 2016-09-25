package com.graphomatic.security

import io.github.javaconductor.gserv.resources.ResourceObject
import org.springframework.stereotype.Service

/**
 * Created by lcollins on 11/13/2015.
 *
 * Thin proxy for use by the UI layer
 *
 */
@Service
class UserResource extends ResourceObject{
	SecurityService securityService

  UserResource(){
    super("/user")
  }

  UserResource(SecurityService securityService ) {
		this(securityService, '/user')
	}

  UserResource(SecurityService securityService, String path ) {
		super (path)
		this.securityService = securityService;
	}

	def start(){
		resource {

			post( '/auth' ) {Map creds ->
				AuthResult result = securityService.authenticate(creds.username, creds.password);
				writeJson result
			}

			post( '/password' ) {Map oldAndNewPswd ->
				AuthResult result = securityService.changePassword(oldAndNewPswd.username,
          oldAndNewPswd.oldPassword,
          oldAndNewPswd.newPassword);
				writeJson result
			}

		}
	}
}
