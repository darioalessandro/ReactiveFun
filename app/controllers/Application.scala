package controllers

import java.util.concurrent.{ Executors}
import play.api.mvc._
import play.api.Play.current
import play.api.libs.ws._
import scala.concurrent.{Promise, ExecutionContext, Future}
import scala.concurrent.duration._


object Application extends Controller {

  implicit val ec = new ExecutionContext {
    val threadPool = Executors.newFixedThreadPool(10)

    def execute(runnable: Runnable) {
      threadPool.submit(runnable)
    }

    def reportFailure(t: Throwable) {}
  }

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def testMultipleRequests(urls:String)=Action.async {

    val timeoutFuture = play.api.libs.concurrent.Promise.timeout("Timeout", 10 seconds)

    val parsedURLs=urls.split(',').toList

    val futures:List[Future[WSResponse]] = parsedURLs map {
      case url:String =>
        val future=WS.url(url)
        future.get()
    }

    Future.firstCompletedOf(Seq(sequence(futures), timeoutFuture)).map {
      case responses:List[WSResponse]=>
        val responsesStatus= responses map (response => response.body)
        val map= (parsedURLs zip responsesStatus).toMap
        Ok(s"Responses $map")
      case t: String => InternalServerError(t)
    } recoverWith {
      case error=>
        Future {
          BadRequest(s"error $error")
        }
    }

  }

  def sequence[T](fs:List[Future[T]]):Future[List[T]] = {
    val successful= Promise[List[T]]()
    successful.success(Nil)
    fs.foldRight(successful.future){
      (f,acc) =>  for {x <- f; xs <- acc} yield  x::xs
    }
  }



}