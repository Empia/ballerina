/**
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
define(['log', 'lodash', 'jquery', 'event_channel', './swagger-holder'],
   function(log, _, $, EventChannel, SwaggerHolder) {

       /**
        * @class SwaggerView
        * @augments EventChannel
        * @constructor
        * @class SwaggerView  Wraps the Swagger Editor for swagger view
        * @param {Object} args - Rendering args for the view
        * @param {String} args.container - selector for div element to render ace editor
        * @param {String} [args.content] - initial content for the editor
        */
       var SwaggerView = function (args) {
           this._options = args;
           if(!_.has(args, 'container')){
               log.error('container is not specified for rendering swagger view.')
           }
           this._container = _.get(args, 'container');
           this._content = _.get(args, 'content');
       };

       SwaggerView.prototype = Object.create(EventChannel.prototype);
       SwaggerView.prototype.constructor = SwaggerView;

       SwaggerView.prototype.render = function () {
           parent.SwaggerHolder = new SwaggerHolder();
           var swaggerEditor = $("#swaggerEditor");
           swaggerEditor.html('<iframe id="se-iframe"  style="border:0px;background: #4a4a4a;" width=100% height="100%"></iframe>');
           document.getElementById('se-iframe').src = swaggerEditor.data("editor-url");
       };

       /**
        * Set the content of swagger editor.
        * @param {String} content - content for the editor.
        *
        */
       SwaggerView.prototype.setContent = function(content){
           parent.SwaggerHolder.setSwaggerAsText(content);
           top.updateSwaggerEditor();
       };

       SwaggerView.prototype.getContent = function(){
           return parent.SwaggerHolder.getSwagger();
       };

       SwaggerView.prototype.show = function(){
           $(this._container).show();
       };

       SwaggerView.prototype.hide = function(){
           $(this._container).hide();
       };

       SwaggerView.prototype.isVisible = function(){
           return  $(this._container).is(':visible')
       };

       return SwaggerView;
   });