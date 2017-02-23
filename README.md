![](https://github.com/covetcode/SelectableContactView/blob/master/picture/GIF.gif)

![](https://github.com/covetcode/SelectableContactView/blob/master/picture/p0.PNG)

![](https://github.com/covetcode/SelectableContactView/blob/master/picture/p2.PNG)

![](https://github.com/covetcode/SelectableContactView/blob/master/picture/p1.PNG)



Usage
-----
copy SelectableContactView.java to your project

copy atts.xml to res/values

```java

   if (SelectableContactView.isSelected()){
                    SelectableContactView.deselect();
                }else {
                    SelectableContactView.select();
                }
                
```xml
      <attr name="text" format="string"/>
        <attr name="textColor" format="color" />
        <attr name="backgroundColor" format="color" />
        <attr name="selectColor" format="color" />
        <attr name="tickColor" format="color" />
        <attr name="shadowColor" format="color" />
        <attr name="showShadow" format="boolean" />
        <attr name="src" format="reference" /> 
```
