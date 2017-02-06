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
define(['lodash', 'jquery', 'log', './compound-statement-view', './../ast/while-statement', 'd3utils', 'd3', 'ballerina/ast/ballerina-ast-factory'],
    function (_, $, log, CompoundStatementView, WhileStatement, D3Utils, d3, BallerinaASTFactory) {

        /**
         * The view to represent a If statement which is an AST visitor.
         * @param {Object} args - Arguments for creating the view.
         * @param {WhileStatement} args.model - The If statement model.
         * @param {Object} args.container - The HTML container to which the view should be added to.
         * @param {Object} args.parent - Parent View, which in this case the parent model
         * @param {Object} [args.viewOptions={}] - Configuration values for the view.
         * @constructor
         */
        var WhileStatementView = function (args) {
            _.set(args, "viewOptions.title.text", "While");
            CompoundStatementView.call(this, args);
        };

        WhileStatementView.prototype = Object.create(CompoundStatementView.prototype);
        WhileStatementView.prototype.constructor = WhileStatementView;

        WhileStatementView.prototype.canVisitWhileStatement = function(){
            return true;
        };

        /**
         * Remove View callback
         * @param {ASTNode} parent - parent node
         * @param {ASTNode} child - child node
         */
        WhileStatementView.prototype.onBeforeModelRemove = function () {
            this._statementContainer.getBoundingBox().off('bottom-edge-moved');
            d3.select("#_" +this._model.id).remove();
            this.getBoundingBox().w(0).h(0);
        };

        /**
         * Override Child remove callback
         * @param {ASTNode} child - removed child
         */
        WhileStatementView.prototype.childRemovedCallback = function (child) {
            if (BallerinaASTFactory.isStatement(child)) {
                this.getStatementContainer().childStatementRemovedCallback(child);
            }
        };

        /**
         * Render the while statement
         */
        WhileStatementView.prototype.render = function (diagramRenderingContext) {
            // Calling super render.
            (this.__proto__.__proto__).render.call(this, diagramRenderingContext);

            // Creating property pane
            var model = this.getModel();
            model.accept(this);
            var editableProperty = {
                propertyType: "text",
                key: "Condition",
                model: model,
                getterMethod: model.getCondition,
                setterMethod: model.setCondition
            };
            this._createPropertyPane({
                                         model: model,
                                         statementGroup: this.getStatementGroup(),
                                         editableProperties: editableProperty
                                     });
            this.listenTo(model, 'update-property-text', this.updateConditionExpression);

            /* Removing all the registered 'child-added' event listeners for this model. This is needed because we
             are not un-registering registered event while the diagram element deletion. Due to that, sometimes we
             are having two or more view elements listening to the 'child-added' event of same model.*/
            model.off('child-added');
            model.on('child-added', function (child) {
                this.visit(child);
            }, this);
        };

        /**
         * @param {BallerinaStatementView} statement
         */
        WhileStatementView.prototype.visit = function (statement) {
            var args = {
                model: statement,
                container: this.getStatementGroup().node(),
                viewOptions: {},
                toolPalette: this.getToolPalette(),
                messageManager: this.messageManager,
                parent: this
            };
            this.getStatementContainer().renderStatement(statement, args);
        };

        WhileStatementView.prototype.updateConditionExpression = function (newCondition, propertyKey) {
            this.getModel().setCondition(newCondition);
        };

        return WhileStatementView;
    });