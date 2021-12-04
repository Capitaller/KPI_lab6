package GoRest

import scala.concurrent.duration._
import io.gatling.core.Predef._
import io.gatling.http.Predef._

object RandomGenerator {
  def randomString(length: Int): String = {
    val SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890"
    val salt = new StringBuilder
    val rnd = new scala.util.Random
    while (salt.length < length) { 
      val index = (rnd.nextFloat() * SALTCHARS.length).asInstanceOf[Int]
      salt.append(SALTCHARS.charAt(index))
    }
    val saltStr = salt.toString
    saltStr
  }

  def randomEmail(): String = randomString(8) + "@gmail.com"

  def randomUserRequest() : String = """{"name":"""".stripMargin + RandomGenerator.randomString(25) + """",
                                   |"gender":"male",
                                   |"email":"""".stripMargin + RandomGenerator.randomEmail() + """",
                                   | "status":"active"} """.stripMargin
}

class UserSimulation extends Simulation {

  val httpProtocol = http
    .baseUrl("https://gorest.co.in")
    .authorizationHeader("Bearer 7853ae1256add79da0c938b4d7946e007d9befec437b8580065f90e12075b2b9")


  val post = scenario("Post User")
    .exec(sessionPost => {
      val sessionPostUpdate = sessionPost.set("postrequest", RandomGenerator.randomUserRequest())
      sessionPostUpdate
    })
    .exec(
      http("Post User")
        .post("/public/v1/users")
        .body(StringBody("${postrequest}")).asJson
    )

  val get = scenario("Get User")
    .exec(sessionPost => {
      val sessionPostUpdate = sessionPost.set("postrequest", RandomGenerator.randomUserRequest())
      sessionPostUpdate
    })
    .exec(
      http("Post User")
        .post("/public/v1/users")
        .body(StringBody("${postrequest}")).asJson
        .check(jsonPath("$.data.id").saveAs("userId"))
    )
    .exitHereIfFailed
    .exec(
      http("Get User")
        .get("/public/v1/users/${userId}")
    )

  val put = scenario("Put User")
    .exec(sessionPost => {
      val sessionPostUpdate = sessionPost.set("postrequest", RandomGenerator.randomUserRequest())
      sessionPostUpdate
    })
    .exec(
      http("Post User")
        .post("/public/v1/users")
        .body(StringBody("${postrequest}")).asJson
        .check(jsonPath("$.data.id").saveAs("userId"))
    )
    .exitHereIfFailed
    .exec(sessionPut => {
      val sessionPutUpdate = sessionPut.set("putrequest", RandomGenerator.randomUserRequest())
      sessionPutUpdate
    })
    .exec(
      http("Put User")
        .put("/public/v1/users/${userId}")
        .body(StringBody("${putrequest}")).asJson
    )

  val delete = scenario("Delete User")
    .exec(sessionPost => {
      val sessionPostUpdate = sessionPost.set("postrequest", RandomGenerator.randomUserRequest())
      sessionPostUpdate
    })
    .exec(
      http("Post User")
        .post("/public/v1/users")
        .body(StringBody("${postrequest}")).asJson
        .check(jsonPath("$.data.id").saveAs("userId"))
    )
    .exitHereIfFailed
    .exec(sessionPut => {
      val sessionPutUpdate = sessionPut.set("putrequest", RandomGenerator.randomUserRequest())
      sessionPutUpdate
    })
    .exec(
      http("Delete User")
        .delete("/public/v1/users/${userId}")
    )

  setUp(post.inject(rampUsers(100).during(30.seconds)).protocols(httpProtocol),
    get.inject(rampUsers(100).during(30.seconds)).protocols(httpProtocol),
    put.inject(rampUsers(100).during(30.seconds)).protocols(httpProtocol),
    delete.inject(rampUsers(100).during(30.seconds)).protocols(httpProtocol))
}
