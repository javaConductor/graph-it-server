package com.graphomatic.oauth

/**
 * Created by lcollins on 11/15/2015.
 */
interface OAuthProfile {
	String getName()
	String authenticate(String clientId, String clientSecret);
	String passThru(String clientId, String clientToken, String method, String url, byte[] data);
}
