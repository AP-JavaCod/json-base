JSON parser

implemented in java 17

# Examples

## JSONObject

create:

```
String json = new JSONObject.JSONBuilder()
				.put("key1", "val")
				.put("key2", "valu2")
				.toJSON()
				.toString();
```

parser:

```
String jsonText = "{\"key1\":\"val\",\"key2\":\"valu2\"}";
JSONObject json = JSONObject.parse(jsonText); //throws JSONParserException
```

method getValue:

JSON:

```
{
	"sender": {
		"isMen" : true,
		"name": "Tom",
		"age": 56 	
	},
	"recipient": {
		"isMen":false,
		"name": "Meg",
		"age": 61
	},
	"message": {
		"time": 1791742201341 ,
		"message": "Hello!"
	},
	"test": [ 1 , 2 , 3 , 4 , 5 ],
	"fos": [ ]

}
```

cod:

```
int age = r.getValue("sender", "age"); //-> age =56
```

## JSONArray

create:

```
String json = JSONArray.instalArray("val1", "val2").toString();
```

parser:

```
String jsonText = "[\"val1\", \"val2\"]";
JSONArray json = JSONArray.parse(josnText); //throws JSONParserException
```

# Connect

maven:

```
<dependency>
  		<groupId>ru.object-cat.jsonstruct</groupId>
  		<artifactId>json-base</artifactId>
		<version>1.0.0</version>
  	</dependency>
```

module java:

```
requires ru.objectcat.base;
```