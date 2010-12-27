/**
 * Copyright (c) 2003-2009, David A. Czarnecki
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the
 *     following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 *     following disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of "David A. Czarnecki" and "blojsom" nor the names of its contributors may be used to
 *     endorse or promote products derived from this software without specific prior written permission.
 * Products derived from this software may not be called "blojsom", nor may "blojsom" appear in their name,
 *     without prior written permission of David A. Czarnecki.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
 * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED
 * AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.blojsom.plugin.weather;

/**
 * Weather
 *
 * @author David czarnecki
 * @author Mark Lussier
 * @version $Id: Weather.java,v 1.3 2008-07-07 19:54:15 czarneckid Exp $
 * @since blojsom 3.0
 */
public class Weather {

    private String _stationCode;
    private String _providerClass;

    /**
     * Default constructor. Not enabled by default.
     */
    public Weather() {
    }

    /**
     * Get the weather station ID for retrieving a feed of weather data
     *
     * @return Weather station ID
     */
    public String getStationCode() {
        return _stationCode;
    }

    /**
     * Set the weather station ID for retrieving a feed of weather data
     *
     * @param stationCode Weather station ID
     */
    public void setStationCode(String stationCode) {
        _stationCode = stationCode;
    }

    /**
     * Retrieve the fully-qualified classname handling the parsing of weather information
     *
     * @return Fully-qualified classname handling parsing of weather information
     */
    public String getProviderClass() {
        return _providerClass;
    }

    /**
     * Set the fully-qualified classname handling the parsing of weather information
     *
     * @param providerClass Fully-qualified classname handling parsing of weather information
     */
    public void setProviderClass(String providerClass) {
        _providerClass = providerClass;
    }
}
