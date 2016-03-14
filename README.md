# ShopifyClientJava
A slightly modified port of a Shopify API Client to Java (ohShopify PHP Adapter).

Tested for GET, POST, PUT, and DELETE operations (Test library included).

Requires only org.json package to work.

To use, create a new ShopifyClient object and call .delete() .get() .post() or .put(). These functions return Map objects. Included also is deleteReturnJSON() putReturnJSON etc for returning JSON objects.
