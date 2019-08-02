#ZK-CONFIG

## zk put value [yml format]
oneProperty: xxValue
twoProperty: yyValue

## yml
zkf:
  enabled: true
  url: ${zkurl}
  propertyPrefix: ${prefixPath}
  
## config bean
@Zkf(WatchKey = "${watchPath}")
public class XxxConfig {
private String oneProperty;
private Integer twoProperty;
}

## Other bean
```
@Component
public class XxxService {
  @Autowrite
  XxxConfig xx;
}
```
