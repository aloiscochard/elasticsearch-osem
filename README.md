# ElasticSearch OSEM

Object Search Engine Mapper for ElasticSearch

## Usage

Configure your model with the annotations:

    import org.elasticsearch.osem.annotations.Indexable;
    import org.elasticsearch.osem.annotations.Searchable;

    @Searchable
    public class Tweet {
        private String message;
        private String user;
        private Date date;

        public String getMessage() { return message; }

        public void setMessage(String message) { this.message = message; }

        public String getUser() { return user; }

        public void setUser(String user) { this.user = user; }

        public Date getDate() { return date; }

        @Indexable(indexName = "post_date")
        public void setDate(Date date) { this.date = date; }
    }

Configure an ObjectContext instance with your(s) class(es):

    import org.elasticsearch.osem.core.ObjectContext;
    import org.elasticsearch.osem.core.ObjectContextFactory;

    ...

    ObjectContext context = ObjectContextFactory.create();
    context.add(Tweet.class);

Then you can write objects to the ElasticSearch client:

    node.client().prepareIndex("twitter", "tweet", "1").setSource(context.write(tweet)).execute().actionGet();


And read them from search hits:

    for (SearchHit hit : searchResponse.getHits()) {
        Tweet t = context.read(hit);
    }
    
You can view a full example in [ObjectContextIntegrationTest.java](https://github.com/aloiscochard/elasticsearch-osem/blob/master/src/test/java/org/elasticsearch/osem/integration/ObjectContextIntegrationTest.java)

## Maven Repository

    <dependencies>
      ...
      <dependency>
        <groupId>org.elasticsearch</groupId>
        <artifactId>elasticsearch-osem</artifactId>
        <version>0.1-SNAPSHOT</version>
      </dependency>
      ...
    </dependencies>

    <repositories>
      ...
      <repository>
        <id>aloiscochard snapshots</id>
        <url>http://orexio.org/~alois/repositories/snapshots</url>
      </repository>
      ...
    </repositories>

