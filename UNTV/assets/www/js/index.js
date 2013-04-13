/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

// var streamUrl;

var app = {
    // Application Constructor
initialize: function() {
    this.bindEvents();
},
    // Bind Event Listeners
    //
    // Bind any events that are required on startup. Common events are:
    // 'load', 'deviceready', 'offline', and 'online'.
bindEvents: function() {
    document.addEventListener('deviceready', this.onDeviceReady, false);
},
    // deviceready Event Handler
    //
    // The scope of 'this' is the event. In order to call the 'receivedEvent'
    // function, we must explicity call 'app.receivedEvent(...);'
onDeviceReady: function() {
    app.receivedEvent('deviceready');
},
    // Update DOM on a Received Event
receivedEvent: function(id) {
    var parentElement = document.getElementById(id);
    var listeningElement = parentElement.querySelector('.listening');
    var receivedElement = parentElement.querySelector('.received');
    
    listeningElement.setAttribute('style', 'display:none;');
    receivedElement.setAttribute('style', 'display:block;');
    
    console.log('Received Event: ' + id);
}
};

// Phonegap Android Video and Audio player plugin

//function playMovie( p_URL, p_isPortrait, p_successFunc, p_errorFunc )
//{
//    cordova.exec(p_successFunc,
//                 p_errorFunc,
//                 "VideoPlayerCommand", "show", [p_URL, p_isPortrait]);
//}
//
//function seturl(intval,url) {
//    streamUrl = url;
//    callPlugin(intval);
//    
//}
//function callPlugin( p_sampleIndex )
//{
//    
//    switch( p_sampleIndex )
//    {
//        case 0:
//            playMovie( streamUrl, 'YES' );
//            break;
//            
//        case 1:
//            playMovie( streamUrl, 'YES' );
//            break;
//            
//    }
//};
//
//function getStreaming(media) {
//    var url;
//     if (media == "untv")
//     {
//        url = "http://www.untvweb.com/api/get_recent_posts/?callback=?&post_type=Streaming";
//     }
//    else
//      {
//        url = "http://www.untvradio.com/api/get_recent_posts/?callback=?&post_type=Streaming";
//      }
//    
//
//    var streamUrl;
//    $.getJSON(url).done(function (response) {
//                        var r = response.posts; //here's your response
//                        streamUrl = r[0].excerpt;
//                        
//                        seturl(0,streamUrl);
//                        });
//}
		