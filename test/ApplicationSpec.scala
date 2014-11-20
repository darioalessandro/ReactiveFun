import controllers.Application._
import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._

import play.api.test._
import play.api.test.Helpers._

/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 * For more information, consult the wiki.
 */
@RunWith(classOf[JUnitRunner])
class ApplicationSpec extends Specification {

  "Application" should {

    "throw a Missing parameter error" in new WithApplication{
      val home = route(FakeRequest(GET, "/testMultipleRequests")).get

      status(home) must equalTo(400)
      contentType(home) must beSome.which(_ == "text/html")
      contentAsString(home) must contain ("Missing parameter")
    }

    "throw a invalid url error" in new WithApplication{
      val home = route(FakeRequest(GET, "/testMultipleRequests?urls=http://www.gsdfsef.com")).get
      status(home) must equalTo(400)
      contentAsString(home) must contain ("error")
    }

    "get all the http responses within 10 seconds" in new WithApplication{
      val home = route(FakeRequest(GET, "/testMultipleRequests?urls=http://www.google.com,http://www.apple.com,http://www.yahoo.com")).get
      status(home) must equalTo(200)
      contentAsString(home) must contain ("Responses")
    }
  }
}
