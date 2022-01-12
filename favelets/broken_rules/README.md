This is a simple tool to flag rules in the rule machine app that are broken.

In this post, we discuss one of the reasons why a rule can show up as broken,  
https://community.hubitat.com/t/feature-request-rm-broken-action-indicator/58162


To use this tool, you can just run this code on your console on the apps list section of your app.

If you want to use it repeatedly, you can create a new bookmark with the following content:
`javascript:(function(){if(window.UBookmarklet!==undefined){UBookmarklet();}else{var scr ='https://raw.githubusercontent.com/luarmr/hubitat/favelet_broken_rules/favelets/broken_rules/broken_rules.js; var oReq = new XMLHttpRequest(); oReq.addEventListener("load", function fLoad() {eval(this.responseText + '\r\n//# sourceURL=' + scr)}); oReq.open("GET", scr); oReq.send(); false;}})();`


To know more about favelets:
https://kb.ucla.edu/articles/what-are-favelets--how-can-i-modify-my-browser-on-the-fly


