package info.whiter4bbit.oauth.scalatra

import scalaz._
import Scalaz._
import net.liftweb.json.scalaz.JsonScalaz._
import net.liftweb.json._
import org.scalatra._
import java.util.Date

trait Scalatraz { self: ScalatraKernel =>
   sealed abstract class ScalatrazError(val message: String, val code: Int = 400)
   case class GenericError(override val message: String) extends ScalatrazError(message)

   implicit def dateJSON(implicit formats: Formats): JSON[Date] = new JSON[Date] {
      def read(json: JValue) = json match {
         case v@JString(x) => formats.dateFormat.parse(x).map((date) => {
	    date.success
	 }).getOrElse(UnexpectedJSONError(v, classOf[JString]).fail.liftFailNel)
	 case x => UnexpectedJSONError(x, classOf[JString]).fail.liftFailNel
      }
      def write(date: Date) = {
         JString(formats.dateFormat.format(date))
      }
   }

   def headerz(key: String): Validation[ScalatrazError, String] = {
      val value = self.request.getHeader(key)
      if (value == null) {
         GenericError("Header not found %s" format key).fail
      } else {
         value.success
      }
   }

   def paramz(key: String): Validation[ScalatrazError, String] = {
      self.params.get(key).map(_.success).getOrElse(GenericError("Param not found %s" format key).fail)
   }   

   def expand(f: => Validation[Any, Any]) = {
      f ||| ((e) => {
         val error = GenericError(e.toString)
	 halt(error.code, error.message)	       
      })
   }

   def getz(route: String)(f: => Validation[Any, Any]) = get(route)(expand(f))
   def postz(route: String)(f: => Validation[Any, Any]) = post(route)(expand(f))
}
