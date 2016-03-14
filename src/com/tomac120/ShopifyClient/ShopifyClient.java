package com.tomac120.ShopifyClient;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;



public class ShopifyClient {
	public String shop_domain;
	private String token;
	private String api_key;
	private String secret;
	private Map<String,String> last_response_headers;
	private boolean isConnectionFailure = false;
	private boolean isSuccess = false;
	private String failureReason = "";
	private String failedResult;
	private int responseCode = -1;

	public ShopifyClient(String shop_domain, String token, String api_key, String secret) {
		this.shop_domain = shop_domain;
		this.token = token;
		this.api_key = api_key;
		this.secret = secret;
	}
	

	public String getAuthorizeUrl(String scope, String redirect_url) {
		String url = "http://{$this->shop_domain}/admin/oauth/authorize?client_id={$this->api_key}&scope=" +URLEncoder.encode(scope);
		if (redirect_url != null && !redirect_url.equals(""))
		{
			url = url + "&redirect_uri=" + URLEncoder.encode(redirect_url);
		}
		return url;
	}

	// Once the User has authorized the app, call this with the code to get the access token
	public String getAccessToken(String code) {
		
		String url = "https://{$this->shop_domain}/admin/oauth/access_token";
		String payload = "client_id={$this->api_key}&client_secret={$this->secret}&code=$code";
		String response = this.curlHttpApiRequest("POST", url, payload);
		if (response != null){
			JSONObject responseJSON = this.decodeJSON(response);
			if (responseJSON != null){
				String access_token = responseJSON.getString("access_token");
				if (access_token != null){
					return access_token;
				}
			}
		}
		return "";
	}

	public Map<String,Object> get(String path, Map<String,String> data) {
		return ShopifyClient.toMapOrNull(this.getReturnJSON(path, data));
	}
	
	public Map<String,Object> delete(String path, Map<String,String> data) {
		return ShopifyClient.toMapOrNull(this.deleteReturnJSON(path, data));
	}

	public Map<String,Object> post(String path, Map<String,Object> data) 
	{
		return ShopifyClient.toMapOrNull(this.postReturnJSON(path, data));
	}
	
	public Map<String,Object> put(String path, Map<String,Object> data) 
	{
		return ShopifyClient.toMapOrNull(this.putReturnJSON(path, data));
	}
	
////////
	public JSONObject getReturnJSON(String path, Map<String,String> data) {
		return this._call_delete_getReturnJSON("GET", path, data);
	}
	
	public JSONObject deleteReturnJSON(String path, Map<String,String> data) {
		return this._call_delete_getReturnJSON("DELETE", path, data);
	}
	
	public JSONObject _call_delete_getReturnJSON(String method, String path, Map<String,String> data)  {
		String url = "https://"+this.shop_domain+path;
		if (method.equals("GET") || method.equals("DELETE")){
			if (data != null){
				String query = this.getQueryFromMap(data);
				url = url+"?"+query;
			}
			return this._call(method, url, null);
		} else {
			throw new RuntimeException("Error API 84");
		}
	}

	public JSONObject postReturnJSON(String path, Map<String,Object> data) 
	{
		String url = "https://"+this.shop_domain+path;
		return this._call("POST", url, data);
	}
	
	public JSONObject putReturnJSON(String path, Map<String,Object> data) 
	{
		String url = "https://"+this.shop_domain+path;
		return this._call("PUT", url, data);
	}
	
	protected JSONObject _call(String method, String url, Map<String,Object> data)
	{
		if (method.equals("GET") || method.equals("DELETE")){
			if (data != null){
				throw new RuntimeException("Error #115, use .GET or .DELETE");
			}
		}
		
		
		String payload;
		if (method.equals("POST") || method.equals("PUT")){
	        JSONObject a = new JSONObject(data);
			payload = a.toString();
		} else {
			payload = "";
		}
		return this._callWithPayload(method, url, payload);
	}
	

	public JSONObject callMethodRAW(String method, String path, String payload) {
		String url = "https://"+this.shop_domain+path;
		return this._callWithPayload(method, url, payload);		
	}
	
	protected JSONObject _callWithPayload(String method, String url, String payload) {
		
	
		//String url = baseurl.ltrim(path, '/');
		List<String> request_headers = new ArrayList<String>();

		if (method.equals("POST") || method.equals("PUT")){
			request_headers.add("Content-Type: application/json; charset=utf-8");
			request_headers.add("Expect:");
		}
		request_headers.add("X-Shopify-Access-Token: "+this.token);
		
		String response = this.curlHttpApiRequest(method, url, payload, request_headers);
		if (response != null){
			JSONObject responseJSON = this.decodeJSON(response);
			if (responseJSON != null){
				return responseJSON;
			}
		}
		return null;
	}
	
	protected JSONObject decodeJSON(String result){
        try {
            JSONObject j = new JSONObject(result);
            return j;
        } catch (JSONException e){
            isSuccess = false;
            failedResult = result;
            failureReason = "JSON Exception (JSON did not parse)";
        }
		return null;
	}
	
	public int callsMade(){

		//return $this->shopApiCallLimitParam(0);
		return 0;
	}
	public int callLimit()
	{
		//return $this->shopApiCallLimitParam(1);
		return 0;
	}

	public int callsLeft(List<String> response_headers)
	{
		//return $this->callLimit() - $this->callsMade();
		return 0;
	}

	public boolean validateSignature(String query)
	{
		return false;/*
		if(!is_array($query) || empty($query['signature']) || !is_string($query['signature']))
			return false;

		foreach($query as $k => $v) {
			if($k == 'signature') continue;
			$signature[] = $k . '=' . $v;
		}

		sort($signature);
		$signature = md5($this->secret . implode('', $signature));

		return $query['signature'] == $signature;*/
	}
	

    private String getQueryFromMap(Map<String,String> params)
    {
        try {
            StringBuilder result = new StringBuilder();
            boolean first = true;

            for (Map.Entry<String, String> entry : params.entrySet()){
                if (first)
                    first = false;
                else
                    result.append("&");

                String key = entry.getKey();
                String value = entry.getValue();
                result.append(URLEncoder.encode(key, "UTF-8"));
                result.append("=");
                result.append(URLEncoder.encode(value, "UTF-8"));
            }

            return result.toString();
        } catch (UnsupportedEncodingException e){
            throw new RuntimeException(e);
        }
    }
    
    private String curlHttpApiRequest(String method, String url_string){
    	return this.curlHttpApiRequest(method, url_string, "");
    }
    
    private String curlHttpApiRequest(String method, String url_string, String payload){
    	return this.curlHttpApiRequest(method, url_string, payload, null);
    }

	private String curlHttpApiRequest(String method, String url_string, String payload, List<String> request_headers)
	{
        try {
            try {
                URL url = new URL(url_string);
                HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(15000);
                try {
                    conn.setRequestMethod(method);
                } catch (Exception e) {
                    // should not happen
                    throw new RuntimeException(e);
                }
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setInstanceFollowRedirects(true);
                
                if (request_headers != null){
                	for (String header_i : request_headers){
                		String[] e = header_i.split(":");
                		if (e.length == 2){
                			conn.addRequestProperty(e[0], e[1].trim());
                		}
                	}
                }


                try {
                    if (method.equals("POST") || method.equals("PUT")) {
                        OutputStream os = conn.getOutputStream();
                        BufferedWriter writer = new BufferedWriter(
                                new OutputStreamWriter(os, "UTF-8"));
                        writer.write(payload);
                        writer.flush();
                        writer.close();
                        os.close();
                    }

                    //conn.connect(); // would otherwise be done by get input stream, but used for status code here
                    String jsonString = this.handleResponseInputStream(conn.getInputStream());
                    String result = jsonString.toString();
                    responseCode = conn.getResponseCode();
                    if (responseCode < 400){
                        try {
                            return result;
                        } catch (Exception e){
                            isSuccess = false;
                            failureReason = e.toString()+" Fitango error 12221";
                        }
                        //String response = conn.getResponseMessage();

                    } else {
                        isSuccess = false;
                        failedResult = result;
                        failureReason = "Response code >= 400";
                    }
                } catch (IOException e) {
                    isConnectionFailure = true;
                    failedResult = this.handleResponseInputStream(conn.getErrorStream());
                }
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        } catch (IOException e){
            isConnectionFailure = true;
        }
		/*
		$url = $this->curlAppendQuery($url, $query);
		$ch = curl_init($url);
		$this->curlSetopts($ch, $method, $payload, $request_headers);
		$response = curl_exec($ch);
		$errno = curl_errno($ch);
		$error = curl_error($ch);
		curl_close($ch);

		if ($errno) throw new ShopifyCurlException($error, $errno);
		list($message_headers, $message_body) = preg_split("/\r\n\r\n|\n\n|\r\r/", $response, 2);
		$this->last_response_headers = $this->curlParseHeaders($message_headers);

		return $message_body;
		*/
        return null;
	}
	
	private String handleResponseInputStream(InputStream is){
        String line;
        StringBuilder jsonString = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        try {
		    while ((line = br.readLine()) != null) {
		        jsonString.append(line);
		    }
        } catch (Exception e){
        	
        }
        return jsonString.toString();
	}

	private String curlAppendQuery(String url, String query){
		return url+"?"+query;
	}

	/*
	private function curlSetopts($ch, $method, $payload, $request_headers)
	{
		curl_setopt($ch, CURLOPT_HEADER, true);
		curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
		curl_setopt($ch, CURLOPT_FOLLOWLOCATION, true);
		curl_setopt($ch, CURLOPT_MAXREDIRS, 3);
		curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, true);
		curl_setopt($ch, CURLOPT_SSL_VERIFYHOST, 2);
		curl_setopt($ch, CURLOPT_USERAGENT, 'ohShopify-php-api-client');
		curl_setopt($ch, CURLOPT_CONNECTTIMEOUT, 30);
		curl_setopt($ch, CURLOPT_TIMEOUT, 30);

		curl_setopt ($ch, CURLOPT_CUSTOMREQUEST, $method);
		if (!empty($request_headers)) curl_setopt($ch, CURLOPT_HTTPHEADER, $request_headers);
		
		if ($method != 'GET' && !empty($payload))
		{
			if (is_array($payload)) $payload = http_build_query($payload);
			curl_setopt ($ch, CURLOPT_POSTFIELDS, $payload);
		}
	}

	private function curlParseHeaders($message_headers)
	{
		$header_lines = preg_split("/\r\n|\n|\r/", $message_headers);
		$headers = array();
		list(, $headers['http_status_code'], $headers['http_status_message']) = explode(' ', trim(array_shift($header_lines)), 3);
		foreach ($header_lines as $header_line)
		{
			list($name, $value) = explode(':', $header_line, 2);
			$name = strtolower($name);
			$headers[$name] = trim($value);
		}

		return $headers;
	}
	
	private function shopApiCallLimitParam($index)
	{
		if ($this->last_response_headers == null)
		{
			throw new Exception('Cannot be called before an API call.');
		}
		$params = explode('/', $this->last_response_headers['http_x_shopify_shop_api_call_limit']);
		return (int) $params[$index];
	}	
	*/
	
	private int shopApiCallLimitParam(int index){
		if (this.last_response_headers != null){
			String header = this.last_response_headers.get("http_x_shopify_shop_api_call_limit");
			if (header != null){
				String[] params = header.split("/");
				if (params.length > index){
					return (int)ShopifyClient.intFromString(params[index]);
				}
			}
		}
		return 0;
	}
	
	private static Map<String,Object> toMapOrNull(JSONObject json){
		if (json != null){
			return ShopifyClient.jsonToMap(json);
		}
		return null;
	}
	

    private static Map<String, Object> jsonToMap(JSONObject json) {
        if(json != JSONObject.NULL) {
            return toMap(json);
        }
        return new HashMap<String, Object>();
    }

    private static Map<String, Object> toMap(JSONObject object) {
        Map<String, Object> map = new HashMap<String, Object>();

        Iterator<String> keysItr = object.keys();
        while(keysItr.hasNext()) {
            try {
                String key = keysItr.next();
                Object value = object.get(key);

                if (value instanceof JSONArray) {
                    value = toList((JSONArray) value);
                } else if (value instanceof JSONObject) {
                    value = toMap((JSONObject) value);
                }
                map.put(key, value);
            } catch (JSONException e){
                // unlikely exception
                throw new RuntimeException(e);
            }
        }
        return map;
    }

    private static List<Object> toList(JSONArray array) {
        List<Object> list = new ArrayList<>();
        for(int i = 0; i < array.length(); i++) {
            try {
                Object value = array.get(i);
                if (value instanceof JSONArray) {
                    value = toList((JSONArray) value);
                } else if (value instanceof JSONObject) {
                    value = toMap((JSONObject) value);
                }
                list.add(value);
            } catch(JSONException e){
                // unlikely exception
                throw new RuntimeException(e);
            }
        }
        return list;
    }
    
    public String getErrorMessage(){
    	return "test";
    }

    public static long intFromString(String str){
        if (str == null){
            return 0;
        }
        try {
            long n = Long.valueOf(str);
            return n;
        } catch(Exception e){
            return 0;
        }
    }
    
}
