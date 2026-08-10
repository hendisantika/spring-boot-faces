## JavaServer Faces with Spring Boot

A small CRUD sample — list, create and delete products — built on Spring Boot 4
and Jakarta Faces 4. It shows how to run a Faces application inside Spring
Boot's embedded Tomcat, with Spring beans acting as the backing beans.

### Stack

| Concern       | Library                                                    |
| ------------- | ---------------------------------------------------------- |
| Spring Boot   | 4.1.0 (Spring Framework 7, embedded Tomcat 11)             |
| Jakarta Faces | Apache MyFaces 4.1.3                                       |
| CDI           | Apache OpenWebBeans 4.1.0                                  |
| Components    | PrimeFaces 15.0.17 (`jakarta` classifier)                  |
| Pretty URLs   | Rewrite / PrettyFaces 10.0.2.Final                         |
| Persistence   | Spring Data JPA (Hibernate 7), HSQLDB 2.7.4, Flyway 13.2.0 |

### Requirements

* JDK 25
* Maven 3.9+

### Running

```
mvn clean spring-boot:run
```

The application is served on <http://localhost:8080/>.

| URL        | Page                                           |
| ---------- | ---------------------------------------------- |
| `/`        | List of products, with a Delete action per row |
| `/product` | Form to add a new product                      |

Both are `@Join` mappings declared on the controllers; Rewrite forwards them to
the underlying `*.jsf` views.

Run the application with `spring-boot:run` rather than from the packaged jar.
Rewrite discovers the `@Join` mappings by scanning `WEB-INF/classes`, so the
build writes its compiler output into `src/main/webapp/WEB-INF/classes`, and
the Facelets views are served from that exploded directory rather than from the
jar.

### Database

Flyway creates the schema and inserts the demo products on first start, into an
HSQLDB file database under `data/`. That directory is not checked in — delete
it to start over from an empty database:

```
rm -rf data && mvn clean spring-boot:run
```

Migrations live in `src/main/resources/db/migration`.

### How it fits together

Faces and Spring Boot need some wiring, because embedded Tomcat does not run
the `ServletContainerInitializer`s that Jakarta EE libraries rely on. All of it
lives in `Application`:

* the OpenWebBeans configuration listener boots CDI, which Faces 4 is specified
  on top of, and runs first
* MyFaces' `StartupServletContextListener` is registered explicitly, right
  after it
* `FacesServlet` is mapped to `*.jsf`, and the Rewrite filter to `/*`

Because the servlet is registered programmatically rather than declared in
`web.xml`, MyFaces cannot see its mapping and would skip initialization; the
`org.apache.myfaces.INITIALIZE_ALWAYS_STANDALONE` context parameter in
`application.properties` tells it to start anyway. Embedded Tomcat never parses
`WEB-INF/web.xml`, so every context parameter is declared there as
`server.servlet.context-parameters.*` instead.

The controllers are ordinary session-scoped Spring `@Component`s. Faces
resolves `#{listProducts}` and `#{productController}` against the Spring
context through `SpringBeanFacesELResolver`, registered in
`WEB-INF/faces-config.xml`.

### Project layout

```
src/main/java/com/auth0/samples/bootfaces/
  Application.java              Spring Boot entry point and Faces/CDI wiring
  controller/                   session-scoped backing beans
  model/Product.java            JPA entity
  persistence/                  Spring Data repository
src/main/resources/
  application.properties        datasource, JPA and servlet context parameters
  db/migration/                 Flyway migrations
src/main/webapp/
  layout.xhtml                  page template
  product/                      list and form views
  WEB-INF/faces-config.xml      Spring EL resolver
  WEB-INF/beans.xml             CDI bean archive marker
```

#### Screenshot

List Products Page

![List Products Page](img/list.png "List Product Page")

Add New Product

![Add New Product](img/add.png "Add New Product")
