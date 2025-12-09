/*

           DO WHAT THE FUCK YOU WANT TO PUBLIC LICENSE
                   Version 2, December 2004

Copyright (C) 2025 Arpan Mandal <me@arpanrec.com>

Everyone is permitted to copy and distribute verbatim or modified
copies of this license document, and changing it is allowed as long
as the name is changed.

           DO WHAT THE FUCK YOU WANT TO PUBLIC LICENSE
  TERMS AND CONDITIONS FOR COPYING, DISTRIBUTION AND MODIFICATION

 0. You just DO WHAT THE FUCK YOU WANT TO.

*/
package com.arpanrec.aphrodite;

public class ApplicationConstants {
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String API_KEY_HEADER = "Aphrodite-API-Key";
    public static final String OPENAPI_SECURITY_SCHEME_NAME = "Aphrodite-API-Key";

    public static final long MAX_API_KEY_MILI_SECONDS = 30L * 24L * 60L * 60L * 1000L;

    public static final String NAMESPACE_HEADER = "AphroditeNamespace";
    public static final String NAMESPACE_DEFAULT = "aphrodite";
    public static final String INIT_ENDPOINT = "aphrodite-init";
    public static final String API_ENDPOINT = "/api/v1";
    public static final String LOGIN_ENDPOINT = "login";
}
