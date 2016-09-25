package com.graphomatic.oauth

import io.github.javaconductor.gserv.resources.ResourceObject
import org.springframework.stereotype.Service

/**
 * Created by lcollins on 11/13/2015.
 *
 * Thin proxy for use by the UI layer
 *
 */
@Service
class OAuthResource extends ResourceObject{
	OAuthResource( ) {
		this('/oauth2/proxy')
	}

	OAuthResource(String path) {
		super (path)
		start(path)
	}

	def start(path){
		resource {

			//use this as proxy for the client OAuth calls
			// on the server, we add tokens to the request and forward anything else.

			get( '/oauth/proxy' ) {

			}
		}
	}
}
