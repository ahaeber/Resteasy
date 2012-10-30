package org.jboss.resteasy.test.nextgen.providers.jackson;

import org.jboss.resteasy.annotations.providers.NoJackson;
import org.jboss.resteasy.annotations.providers.jaxb.json.BadgerFish;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.test.BaseResourceTest;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import static org.jboss.resteasy.test.TestPortProvider.generateBaseUrl;
import static org.jboss.resteasy.test.TestPortProvider.generateURL;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class JacksonTest extends BaseResourceTest
{
   public static class Product
   {
      protected String name;

      protected int id;

      public Product()
      {
      }

      public Product(int id, String name)
      {
         this.id = id;
         this.name = name;
      }

      public String getName()
      {
         return name;
      }

      public void setName(String name)
      {
         this.name = name;
      }

      public int getId()
      {
         return id;
      }

      public void setId(int id)
      {
         this.id = id;
      }
   }

   @XmlRootElement(name = "product")
   @NoJackson
   @XmlAccessorType(XmlAccessType.FIELD)
   public static class XmlProduct
   {
      @XmlAttribute
      protected String name;

      @XmlAttribute
      protected int id;

      public XmlProduct()
      {
      }

      public XmlProduct(int id, String name)
      {
         this.id = id;
         this.name = name;
      }

      public String getName()
      {
         return name;
      }

      public void setName(String name)
      {
         this.name = name;
      }

      public int getId()
      {
         return id;
      }

      public void setId(int id)
      {
         this.id = id;
      }
   }

   @Path("/products")
   public interface JacksonProxy
   {
      @GET
      @Produces("application/json")
      @Path("{id}")
      Product getProduct();

      @GET
      @Produces("application/json")
      JacksonTest.Product[] getProducts();

      @POST
      @Produces("application/foo+json")
      @Consumes("application/foo+json")
      @Path("{id}")
      Product post(@PathParam("id") int id, Product p);
   }


   @Path("/products")
   public static class JacksonService
   {

      @GET
      @Produces("application/json")
      @Path("{id}")
      public Product getProduct()
      {
         return new Product(333, "Iphone");
      }

      @GET
      @Produces("application/json")
      public Product[] getProducts()
      {

         Product[] products = {new Product(333, "Iphone"), new Product(44, "macbook")};
         return products;
      }

      @POST
      @Produces("application/foo+json")
      @Consumes("application/foo+json")
      @Path("{id}")
      public Product post(Product p)
      {
         return p;
      }

   }


   @Path("/xml/products")
   public static class XmlService
   {

      @GET
      @Produces("application/json")
      @Path("{id}")
      @BadgerFish
      public XmlProduct getProduct()
      {
         return new XmlProduct(333, "Iphone");
      }

      @GET
      @Produces("application/json")
      @NoJackson
      public XmlProduct[] getProducts()
      {

         XmlProduct[] products = {new XmlProduct(333, "Iphone"), new XmlProduct(44, "macbook")};
         return products;
      }

   }

   private static ResteasyClient client;

   @BeforeClass
   public static void setUp() throws Exception
   {
      dispatcher.getRegistry().addPerRequestResource(JacksonService.class);
      dispatcher.getRegistry().addPerRequestResource(XmlService.class);
      client = new ResteasyClient();
   }


   @AfterClass
   public static void shutdown() throws Exception
   {
      client.close();
   }

   @Test
   public void testJacksonString() throws Exception
   {
      WebTarget target = client.target(generateURL("/products/333"));
      Response response = target.request().get();
      System.out.println(response.readEntity(String.class));
      Assert.assertEquals(200, response.getStatus());
      Assert.assertEquals("{\"name\":\"Iphone\",\"id\":333}", response.getEntity());
      response.close();

      target = client.target(generateURL("/products"));
      Response response2 = target.request().get();
      System.out.println(response2.readEntity(String.class));
      Assert.assertEquals(200, response2.getStatus());
      Assert.assertEquals("[{\"name\":\"Iphone\",\"id\":333},{\"name\":\"macbook\",\"id\":44}]", response2.getEntity());
      response2.close();

   }

   @Test
   public void testXmlString() throws Exception
   {
      WebTarget target = client.target(generateURL("/xml/products/333"));
      Response response = target.request().get();
      System.out.println(response.readEntity(String.class));
      Assert.assertEquals(200, response.getStatus());
      Assert.assertTrue(((String)response.getEntity()).startsWith("{\"product"));
      response.close();


      target = client.target(generateURL("/xml/products"));
      Response response2 = target.request().get();
      System.out.println(response2.readEntity(String.class));
      Assert.assertEquals(200, response2.getStatus());
      Assert.assertTrue(((String)response2.getEntity()).startsWith("[{\"product"));
      response2.close();
   }

   @Test
   public void testJackson() throws Exception
   {
      WebTarget target = client.target(generateURL("/products/333"));
      Response response = target.request().get();
      Product p = response.readEntity(Product.class);
      Assert.assertEquals(333, p.getId());
      Assert.assertEquals("Iphone", p.getName());
      response.close();
      target = client.target(generateURL("/products"));
      Response response2 = target.request().get();
      System.out.println(response2.readEntity(String.class));
      Assert.assertEquals(200, response2.getStatus());
      response2.close();

      target = client.target(generateURL("/products/333"));
      response = target.request().post(Entity.entity(p, "application/foo+json"));
      p = response.readEntity(Product.class);
      Assert.assertEquals(333, p.getId());
      Assert.assertEquals("Iphone", p.getName());
      response.close();


   }

   @Test
   public void testJacksonProxy() throws Exception
   {
      JacksonProxy proxy = client.target(generateBaseUrl()).proxy(JacksonProxy.class);
      Product p = new Product(1, "Stuff");
      p = proxy.post(1, p);
      Assert.assertEquals(1, p.getId());
      Assert.assertEquals("Stuff", p.getName());
   }
}