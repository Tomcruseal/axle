package org.pingel.http

/*

import blueeyes.concurrent.Future
import blueeyes.BlueEyesServer
import net.lag.configgy.ConfigMap
import blueeyes.json.JsonAST._
import blueeyes.core.http.HttpStatusCodes._
import blueeyes.BlueEyesServiceBuilder
import blueeyes.core.http.combinators.HttpRequestCombinators
import blueeyes.persistence.mongo.MongoImplicits._
import blueeyes.core.http.{HttpRequest, HttpResponse}
import blueeyes.core.http.MimeTypes._
import blueeyes.core.data.{ByteChunk, BijectionsChunkJson}
import blueeyes.persistence.mongo.{MongoFilterAll, Mongo, MongoFilter}
import blueeyes.json.{JPathField, JPath}
import blueeyes.persistence.mongo.MockMongo

object FooServer extends BlueEyesServer with FooService {
	lazy val mongo = new MockMongo()
	// override def main(args: Array[String]) = super.main(Array("--configFile", "blueeyes.conf"))
}

trait FooService extends BlueEyesServiceBuilder with HttpRequestCombinators with BijectionsChunkJson {

	def mongo: Mongo

	val fooListService = service("foolist", "1.0.0") {
	healthMonitor { monitor => context =>
	startup {
		FooConfig(context.config, mongo)
		// config.database[JNothing.type](ensureUniqueIndex("contacts.name").on(config.collection, "name"))
		// config
	} ->
	request { fooConfig: FooConfig =>

	import fooConfig._

	path("/foos"){
		produce(application/json) {
			get { request: HttpRequest[ByteChunk] =>
				val contacts = database(select(".name").from(collection)) map {
					records => JArray(records.flatMap(row => (row \\ "name").value).toList)
				}
				contacts.map(v => HttpResponse[JValue](content=Some(v)))
				// HttpResponse[JValue](content=Some(JArray(List(JString("abc"), JString("def")))))
			}
		} ~
		jvalue{
			post {
				refineContentType[JValue, JObject] { request => {
					database[JNothing.type](insert(request.content.get).into(collection))
					HttpResponse[JValue](status = OK, content = Some(JString("inserted")))
					}
				}
			}
		} ~
		path("/'name") {
			produce(application/json) {
				get { request: HttpRequest[ByteChunk] =>
					//val contact = database(selectOne().from(collection).where("name" === request.parameters('name)))
					//contact.map(v => HttpResponse[JValue](content=v, status=if (!v.isEmpty) OK else NotFound))
					HttpResponse[JValue](status = OK, content = Some(JString("name is " + request.parameters('name))))
				} ~
				delete { request: HttpRequest[ByteChunk] =>
					database[JNothing.type](remove.from(collection).where("name" === request.parameters('name)))
					HttpResponse[JValue](status = OK, content = Some(JString("deleted " + request.parameters('name))))
				}
			}
		} ~
		path("/search") {
			jvalue{
				post {
					refineContentType[JValue, JObject] { request =>
					  	val contacts = searchContacts(request.content, fooConfig)
					  	contacts.map(v => HttpResponse[JValue](content=Some(JArray(v))))
					}
				}
			}
		}
	}
	} ->
	shutdown { fooConfig: FooConfig =>
	// Nothing to do
	}
	}
}

private def searchContacts(filterJObject: Option[JObject], config: FooConfig): Future[List[JString]] = {
		createFilter(filterJObject).map { filter =>
		val nameJObject = config.database(select().from(config.collection).where(filter)).map(_.toList)
		nameJObject.map(_.map(_ \ "name" -->? classOf[JString]).filter(_ != None).map(_.get))
		}.getOrElse(Future.lift(Nil))
}

private def createFilter(filterJObject: Option[JObject]) = filterJObject.map {
	_.flatten.collect {
	case f @ JField(_, JString(_)) => f
	}.foldLeft(MongoFilterAll.asInstanceOf[MongoFilter]) { (filter, field) =>
	filter && field.name === field.value.asInstanceOf[JString].value
	}
}
}

case class FooConfig(config: ConfigMap, mongo: Mongo){
	val database   = mongo.database(config.getString("mongo.database.contacts").getOrElse("mb"))
	val collection = config.getString("mongo.collection.foos").getOrElse("foos")
}
*/
