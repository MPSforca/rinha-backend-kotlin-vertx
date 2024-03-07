package com.sforca.rinha.http.validation

import io.vertx.core.Vertx
import io.vertx.ext.web.validation.RequestPredicate
import io.vertx.ext.web.validation.ValidationHandler
import io.vertx.ext.web.validation.builder.Bodies
import io.vertx.ext.web.validation.builder.Parameters
import io.vertx.ext.web.validation.builder.ValidationHandlerBuilder
import io.vertx.json.schema.SchemaParser
import io.vertx.json.schema.SchemaRouter
import io.vertx.json.schema.SchemaRouterOptions
import io.vertx.json.schema.common.dsl.Keywords
import io.vertx.json.schema.common.dsl.ObjectSchemaBuilder
import io.vertx.json.schema.common.dsl.Schemas.enumSchema
import io.vertx.json.schema.common.dsl.Schemas.intSchema
import io.vertx.json.schema.common.dsl.Schemas.objectSchema
import io.vertx.json.schema.common.dsl.Schemas.stringSchema

class TransactionValidationHandler(private val vertx: Vertx) {
    fun saveRequestValidationHandler(): ValidationHandler =
        ValidationHandlerBuilder
            .create(schemaParser())
            .predicate(RequestPredicate.BODY_REQUIRED)
            .pathParameter(Parameters.param("id", intSchema()))
            .body(Bodies.json(saveTransactionRequestSchema()))
            .build()

    private fun schemaParser(): SchemaParser = SchemaParser.createDraft7SchemaParser(SchemaRouter.create(vertx, SchemaRouterOptions()))

    private fun saveTransactionRequestSchema(): ObjectSchemaBuilder =
        objectSchema()
            .requiredProperty("valor", intSchema())
            .requiredProperty("tipo", enumSchema("c", "d"))
            .requiredProperty("descricao", stringSchema().with(Keywords.minLength(1)).with(Keywords.maxLength(10)))
}
