package com.graphomatic.oauth

/**
 * Created by lcollins on 11/15/2015.
 */
class FacebookProfile implements  OAuthProfile {
	@Override
	String getName() {
		return "fb"
	}

	@Override
	String authenticate(String clientId, String clientSecret) {



		return null
	}

	@Override
	String passThru(String clientId, String clientToken, String method, String url, byte[] data) {
		return null
	}
}
