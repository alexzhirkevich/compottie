# List of After Effect expressions supported by Compottie

## Global
- thisLayer
- thisProperty
- time
- value
- comp(name)

## Time Conversion
- framesToTime()
- timeToFrames()

## Vector Math
- add()
- clamp()
- cross()
- div()
- dot()
- length()
- lookAt()
- mul()
- normalize()
- sub()
- degreesToRadians()
- radiansToDegrees()

## Random Numbers

- gaussRandom()
- gaussRandom(minValOrArray, maxValOrArray)
- noise(valOrArray)
- random()
- random(minValOrArray, maxValOrArray)
- seedRandom()

## Interpolation

- linear(t, tMin, tMax, value1, value2)
- linear(t, value1, value2)
- ease(t, tMin, tMax, value1, value2)
- ease(t, value1, value2)
- easeIn(t, tMin, tMax, value1, value2)
- easeIn(t, value1, value2)
- easeOut(t, tMin, tMax, value1, value2)
- easeOut(t, value1, value2)

## Color Conversion

- rgbToHsl()
- hslToRgb()
- hexToRgb()

## Composition

- displayStartTime
- duration
- frameDuration
- width
- height
- name
- numLayers
- layer()

## Layer
- effect()
- mask()
- active
- enabled
- audioActive (always false)
- hasAudio (always false)
- hasVideo (always false) 
- hasParent
- width
- height
- index
- inPoint
- outPoint
- parent
- startTime
- anchorPoint
- marker
- name
- opacity
- position
- rotation
- scale
- timeRemap
- toComp()
- fromComp()
- toWorld()
- fromWorld()
- text

## Effect
- param()

## Mask

- invert
- maskExpansion
- maskFeather
- maskOpacity
- maskPath

## Property
- name
- numKeys
- propertyIndex
- value
- key()
- loopIn()
- loopInDuration()
- loopOut()
- loopOutDuration()
- nearestKey()
- propertyGroup(1)
- smooth()
- temporalWiggle()
- valueAtTime()
- wiggle()
