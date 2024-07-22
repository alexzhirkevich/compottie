# Dynamic Properties

Dynamic properties are used to update animation properties at runtime. Some reasons you may want to do this are:
- Change colors for day/night or other app theme;
- Localize animation text;
- Change the progress of a specific layer to show download progress;
- Change the size and position of something in response to a gesture.

Dynamic properties are created with `rememberLottieDynamicProperties`

```kotlin
val painter = rememberLottiePainter(
    composition = composition,
    dynamicProperties = rememberLottieDynamicProperties {
        shapeLayer("Precomposition 1", "Shape Layer 4") {
            transform {
                rotation { current -> current * progress }
            }
            fill("Group 1", "Fill 4") {
                color { Color.Red }
                alpha { .5f }
            }
            group("Group 4") {
                ellipse("Ellipse 1") {
                    // configure size, position of the ellipse named "Ellipse 1"
                }
                stroke("Ellipse 1 Stroke") {
                    // configure stroke named "Ellipse 1 Stroke" in the same group
                }
            }
        }
    }
) 
```

For a person that isn't familiar with After Effects / [Lottie JSON schema](https://lottiefiles.github.io/lottie-docs/schema/) this might seem scary, but it is pretty simple.

# Lottie Layers

Each Lottie animation consists of *Layers*. 
This can be *Shape Layer* (combination of variours vector shapes), *Image Layer* (raster image - embedded or external). 
There is also special type of layer called *Precomposition Layer*. This layer holds a group of other layers.

If you've just got a Lottie JSON file without AE project and need to easily inspect it layer by layer, you can use a [LottieFiles JSON editor](https://lottiefiles.github.io/lottie-docs/playground/json_editor/)

For the above example assume we have the following structure

```json
{
  "assets" : [
    { // precomposition asset that holds layers
      "id" : "precomposition_1"
      "layers": [
        {
          "nm" : "Shape Layer 4"
          "ty": 4, // our shape layer inside precomposition
        },
      ]
   }
  ],
  "layers": [
    { 
      "refId" : "precomposition_1",
      "nm": "Precomposition 1",
      "ty": 0, // precomposition layer
    },
    {
      "nm" : "Some Other Layer",
      "ty": 4,
    }
  ]
}
```

According to this structure, the keypath for *Shape Layer 4* will be `["Precomposition 1", "Shape Layer 4"]`.
Thats why we have 
```kotlin
shapeLayer("Precomposition 1", "Shape Layer 4") {

}
```
Precompositions can be nested so the keypath may be long. 

## Shape Layer

Now when we found the shape layer, let's have a look on it's shapes. The structure of this shape layer will look like that:

```json
{
  "nm" : "Shape Layer 4",
  "shapes" : [
    { 
      "nm" : "Group 4", 
      "ty": "gr", // group shape that literally groups other shapes
      "it" : [
        { 
          "nm" : "Ellipse 1",
          "ty": "el" // ellipse shape
        },
        { 
          "nm" : "Ellipse 1 Stroke",
          "ty": "st" // ellipse stroke/fill is also a shape
        }
      ]
    }
  ]
}
```

The keypaths for the *Ellipse 1* and its stroke relative to the layer they are in will be `["Group 4", "Ellipse 1"]` and `["Group 4", "Ellipse 1 Stroke"]`.
Thats why we have 

```kotlin
shapeLayer("Precomposition 1", "Shape Layer 4") {
    ellipse("Group 4", "Ellipse 1") {}
    stroke("Group 4", "Ellipse 1 Stroke") {}
}
```
or

```kotlin
shapeLayer("Precomposition 1", "Shape Layer 4") {
  group("Group 4") {
      ellipse("Ellipse 1") {}
      stroke("Ellipse 1 Stroke") {}
  }
}
```

Just like precompositions, groups can be nested. So the keypath to the desired shape may lay throught the large amount of groups
