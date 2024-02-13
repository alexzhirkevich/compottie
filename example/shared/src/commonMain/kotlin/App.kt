import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.alexzhirkevich.compottie.LottieAnimation
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.LottieConstants
import io.github.alexzhirkevich.compottie.animateLottieCompositionAsState
import io.github.alexzhirkevich.compottie.rememberLottieComposition
import io.github.alexzhirkevich.compottie.rememberLottiePainter

private const val lottieData = """
{"v":"4.10.1","fr":60,"ip":0,"op":120,"w":800,"h":800,"nm":"loading_animation","ddd":0,"assets":[],"layers":[{"ddd":0,"ind":1,"ty":4,"nm":"Shape Layer 5","sr":1,"ks":{"o":{"a":1,"k":[{"i":{"x":[0.667],"y":[1]},"o":{"x":[0.333],"y":[0]},"n":["0p667_1_0p333_0"],"t":23,"s":[20],"e":[100]},{"i":{"x":[0.667],"y":[1]},"o":{"x":[0.333],"y":[0]},"n":["0p667_1_0p333_0"],"t":34,"s":[100],"e":[20]},{"t":69}],"ix":11},"r":{"a":0,"k":0,"ix":10},"p":{"a":0,"k":[400,400,0],"ix":2},"a":{"a":0,"k":[0,0,0],"ix":1},"s":{"a":0,"k":[100,100,100],"ix":6}},"ao":0,"shapes":[{"ty":"gr","it":[{"d":1,"ty":"el","s":{"a":1,"k":[{"i":{"x":[0,0],"y":[1,1]},"o":{"x":[0.333,0.333],"y":[0,0]},"n":["0_1_0p333_0","0_1_0p333_0"],"t":23,"s":[400,400],"e":[440,440]},{"i":{"x":[0.009,0.009],"y":[1,1]},"o":{"x":[0.333,0.333],"y":[0,0]},"n":["0p009_1_0p333_0","0p009_1_0p333_0"],"t":34,"s":[440,440],"e":[400,400]},{"t":59}],"ix":2},"p":{"a":0,"k":[0,0],"ix":3},"nm":"Ellipse Path 1","mn":"ADBE Vector Shape - Ellipse","hd":false},{"ty":"tr","p":{"a":0,"k":[0,0],"ix":2},"a":{"a":0,"k":[0,0],"ix":1},"s":{"a":0,"k":[100,100],"ix":3},"r":{"a":0,"k":0,"ix":6},"o":{"a":0,"k":100,"ix":7},"sk":{"a":0,"k":0,"ix":4},"sa":{"a":0,"k":0,"ix":5},"nm":"Transform"}],"nm":"Ellipse 1","np":4,"cix":2,"ix":1,"mn":"ADBE Vector Group","hd":false},{"ty":"gs","o":{"a":0,"k":100,"ix":9},"w":{"a":1,"k":[{"i":{"x":[0.833],"y":[0.833]},"o":{"x":[0.167],"y":[0.167]},"n":["0p833_0p833_0p167_0p167"],"t":23,"s":[5],"e":[10]},{"i":{"x":[0.833],"y":[0.833]},"o":{"x":[0.167],"y":[0.167]},"n":["0p833_0p833_0p167_0p167"],"t":34,"s":[10],"e":[5]},{"t":59}],"ix":10},"g":{"p":3,"k":{"a":0,"k":[0,0,0.627,1,0.5,0.496,0.314,1,1,0.992,0,1],"ix":8}},"s":{"a":0,"k":[0,0],"ix":4},"e":{"a":0,"k":[100,0],"ix":5},"t":1,"lc":1,"lj":1,"ml":4,"nm":"Gradient Stroke 1","mn":"ADBE Vector Graphic - G-Stroke","hd":false}],"ip":0,"op":120,"st":0,"bm":0},{"ddd":0,"ind":2,"ty":4,"nm":"Shape Layer 4","sr":1,"ks":{"o":{"a":1,"k":[{"i":{"x":[0.667],"y":[1]},"o":{"x":[0.333],"y":[0]},"n":["0p667_1_0p333_0"],"t":16,"s":[20],"e":[100]},{"i":{"x":[0.667],"y":[1]},"o":{"x":[0.333],"y":[0]},"n":["0p667_1_0p333_0"],"t":27,"s":[100],"e":[20]},{"t":62}],"ix":11},"r":{"a":0,"k":0,"ix":10},"p":{"a":0,"k":[400,400,0],"ix":2},"a":{"a":0,"k":[0,0,0],"ix":1},"s":{"a":0,"k":[100,100,100],"ix":6}},"ao":0,"shapes":[{"ty":"gr","it":[{"d":1,"ty":"el","s":{"a":1,"k":[{"i":{"x":[0.667,0.667],"y":[1,1]},"o":{"x":[0.333,0.333],"y":[0,0]},"n":["0p667_1_0p333_0","0p667_1_0p333_0"],"t":16,"s":[320,320],"e":[360,360]},{"i":{"x":[0.025,0.025],"y":[1,1]},"o":{"x":[0.333,0.333],"y":[0,0]},"n":["0p025_1_0p333_0","0p025_1_0p333_0"],"t":27,"s":[360,360],"e":[320,320]},{"t":52}],"ix":2},"p":{"a":0,"k":[0,0],"ix":3},"nm":"Ellipse Path 1","mn":"ADBE Vector Shape - Ellipse","hd":false},{"ty":"tr","p":{"a":0,"k":[0,0],"ix":2},"a":{"a":0,"k":[0,0],"ix":1},"s":{"a":0,"k":[100,100],"ix":3},"r":{"a":0,"k":0,"ix":6},"o":{"a":0,"k":100,"ix":7},"sk":{"a":0,"k":0,"ix":4},"sa":{"a":0,"k":0,"ix":5},"nm":"Transform"}],"nm":"Ellipse 1","np":3,"cix":2,"ix":1,"mn":"ADBE Vector Group","hd":false},{"ty":"gs","o":{"a":0,"k":100,"ix":9},"w":{"a":1,"k":[{"i":{"x":[0.833],"y":[0.833]},"o":{"x":[0.167],"y":[0.167]},"n":["0p833_0p833_0p167_0p167"],"t":16,"s":[5],"e":[10]},{"i":{"x":[0.833],"y":[0.833]},"o":{"x":[0.167],"y":[0.167]},"n":["0p833_0p833_0p167_0p167"],"t":27,"s":[10],"e":[5]},{"t":52}],"ix":10},"g":{"p":3,"k":{"a":0,"k":[0,0,0.627,1,0.5,0.496,0.314,1,1,0.992,0,1],"ix":8}},"s":{"a":0,"k":[0,0],"ix":4},"e":{"a":0,"k":[100,0],"ix":5},"t":1,"lc":1,"lj":1,"ml":4,"nm":"Gradient Stroke 1","mn":"ADBE Vector Graphic - G-Stroke","hd":false}],"ip":0,"op":120,"st":0,"bm":0},{"ddd":0,"ind":3,"ty":4,"nm":"Shape Layer 3","sr":1,"ks":{"o":{"a":1,"k":[{"i":{"x":[0.667],"y":[1]},"o":{"x":[0.333],"y":[0]},"n":["0p667_1_0p333_0"],"t":9,"s":[20],"e":[100]},{"i":{"x":[0.667],"y":[1]},"o":{"x":[0.333],"y":[0]},"n":["0p667_1_0p333_0"],"t":20,"s":[100],"e":[20]},{"t":55}],"ix":11},"r":{"a":0,"k":0,"ix":10},"p":{"a":0,"k":[400,400,0],"ix":2},"a":{"a":0,"k":[0,0,0],"ix":1},"s":{"a":0,"k":[100,100,100],"ix":6}},"ao":0,"shapes":[{"ty":"gr","it":[{"d":1,"ty":"el","s":{"a":1,"k":[{"i":{"x":[0.667,0.667],"y":[1,1]},"o":{"x":[0.333,0.333],"y":[0,0]},"n":["0p667_1_0p333_0","0p667_1_0p333_0"],"t":9,"s":[240,240],"e":[280,280]},{"i":{"x":[0.051,0.051],"y":[1,1]},"o":{"x":[0.333,0.333],"y":[0,0]},"n":["0p051_1_0p333_0","0p051_1_0p333_0"],"t":20,"s":[280,280],"e":[240,240]},{"t":45}],"ix":2},"p":{"a":0,"k":[0,0],"ix":3},"nm":"Ellipse Path 1","mn":"ADBE Vector Shape - Ellipse","hd":false},{"ty":"tr","p":{"a":0,"k":[0,0],"ix":2},"a":{"a":0,"k":[0,0],"ix":1},"s":{"a":0,"k":[100,100],"ix":3},"r":{"a":0,"k":0,"ix":6},"o":{"a":0,"k":100,"ix":7},"sk":{"a":0,"k":0,"ix":4},"sa":{"a":0,"k":0,"ix":5},"nm":"Transform"}],"nm":"Ellipse 1","np":3,"cix":2,"ix":1,"mn":"ADBE Vector Group","hd":false},{"ty":"gs","o":{"a":0,"k":100,"ix":9},"w":{"a":1,"k":[{"i":{"x":[0.833],"y":[0.833]},"o":{"x":[0.167],"y":[0.167]},"n":["0p833_0p833_0p167_0p167"],"t":9,"s":[5],"e":[10]},{"i":{"x":[0.833],"y":[0.833]},"o":{"x":[0.167],"y":[0.167]},"n":["0p833_0p833_0p167_0p167"],"t":20,"s":[10],"e":[5]},{"t":45}],"ix":10},"g":{"p":3,"k":{"a":0,"k":[0,0,0.627,1,0.5,0.496,0.314,1,1,0.992,0,1],"ix":8}},"s":{"a":0,"k":[0,0],"ix":4},"e":{"a":0,"k":[100,0],"ix":5},"t":1,"lc":1,"lj":1,"ml":4,"nm":"Gradient Stroke 1","mn":"ADBE Vector Graphic - G-Stroke","hd":false}],"ip":0,"op":120,"st":0,"bm":0},{"ddd":0,"ind":4,"ty":4,"nm":"Shape Layer 2","sr":1,"ks":{"o":{"a":1,"k":[{"i":{"x":[0.667],"y":[1]},"o":{"x":[0.333],"y":[0]},"n":["0p667_1_0p333_0"],"t":2,"s":[20],"e":[100]},{"i":{"x":[0.667],"y":[1]},"o":{"x":[0.333],"y":[0]},"n":["0p667_1_0p333_0"],"t":13,"s":[100],"e":[20]},{"t":48}],"ix":11},"r":{"a":0,"k":0,"ix":10},"p":{"a":0,"k":[400,400,0],"ix":2},"a":{"a":0,"k":[0,0,0],"ix":1},"s":{"a":0,"k":[100,100,100],"ix":6}},"ao":0,"shapes":[{"ty":"gr","it":[{"d":1,"ty":"el","s":{"a":1,"k":[{"i":{"x":[0.667,0.667],"y":[1,1]},"o":{"x":[0.333,0.333],"y":[0,0]},"n":["0p667_1_0p333_0","0p667_1_0p333_0"],"t":2,"s":[160,160],"e":[200,200]},{"i":{"x":[0.034,0.034],"y":[1,1]},"o":{"x":[0.333,0.333],"y":[0,0]},"n":["0p034_1_0p333_0","0p034_1_0p333_0"],"t":13,"s":[200,200],"e":[160,160]},{"t":38}],"ix":2},"p":{"a":0,"k":[0,0],"ix":3},"nm":"Ellipse Path 1","mn":"ADBE Vector Shape - Ellipse","hd":false},{"ty":"tr","p":{"a":0,"k":[0,0],"ix":2},"a":{"a":0,"k":[0,0],"ix":1},"s":{"a":0,"k":[100,100],"ix":3},"r":{"a":0,"k":0,"ix":6},"o":{"a":0,"k":100,"ix":7},"sk":{"a":0,"k":0,"ix":4},"sa":{"a":0,"k":0,"ix":5},"nm":"Transform"}],"nm":"Ellipse 1","np":3,"cix":2,"ix":1,"mn":"ADBE Vector Group","hd":false},{"ty":"gs","o":{"a":0,"k":100,"ix":9},"w":{"a":1,"k":[{"i":{"x":[0.833],"y":[0.833]},"o":{"x":[0.167],"y":[0.167]},"n":["0p833_0p833_0p167_0p167"],"t":2,"s":[5],"e":[10]},{"i":{"x":[0.833],"y":[0.833]},"o":{"x":[0.167],"y":[0.167]},"n":["0p833_0p833_0p167_0p167"],"t":13,"s":[10],"e":[5]},{"t":38}],"ix":10},"g":{"p":3,"k":{"a":0,"k":[0,0,0.627,1,0.5,0.496,0.314,1,1,0.992,0,1],"ix":8}},"s":{"a":0,"k":[0,0],"ix":4},"e":{"a":0,"k":[100,0],"ix":5},"t":1,"lc":1,"lj":1,"ml":4,"nm":"Gradient Stroke 1","mn":"ADBE Vector Graphic - G-Stroke","hd":false}],"ip":0,"op":120,"st":0,"bm":0},{"ddd":0,"ind":5,"ty":4,"nm":"Shape Layer 1","sr":1,"ks":{"o":{"a":1,"k":[{"i":{"x":[0.667],"y":[1]},"o":{"x":[0.333],"y":[0]},"n":["0p667_1_0p333_0"],"t":0,"s":[20],"e":[100]},{"i":{"x":[0.667],"y":[1]},"o":{"x":[0.333],"y":[0]},"n":["0p667_1_0p333_0"],"t":11,"s":[100],"e":[20]},{"t":46}],"ix":11},"r":{"a":0,"k":0,"ix":10},"p":{"a":0,"k":[400,400,0],"ix":2},"a":{"a":0,"k":[0,0,0],"ix":1},"s":{"a":0,"k":[100,100,100],"ix":6}},"ao":0,"shapes":[{"ty":"gr","it":[{"d":1,"ty":"el","s":{"a":1,"k":[{"i":{"x":[0.667,0.667],"y":[1,1]},"o":{"x":[0.333,0.333],"y":[0,0]},"n":["0p667_1_0p333_0","0p667_1_0p333_0"],"t":0,"s":[80,80],"e":[120,120]},{"i":{"x":[0,0],"y":[1,1]},"o":{"x":[0.333,0.333],"y":[0,0]},"n":["0_1_0p333_0","0_1_0p333_0"],"t":11,"s":[120,120],"e":[80,80]},{"t":36}],"ix":2},"p":{"a":0,"k":[0,0],"ix":3},"nm":"Ellipse Path 1","mn":"ADBE Vector Shape - Ellipse","hd":false},{"ty":"tr","p":{"a":0,"k":[0,0],"ix":2},"a":{"a":0,"k":[0,0],"ix":1},"s":{"a":0,"k":[100,100],"ix":3},"r":{"a":0,"k":0,"ix":6},"o":{"a":0,"k":100,"ix":7},"sk":{"a":0,"k":0,"ix":4},"sa":{"a":0,"k":0,"ix":5},"nm":"Transform"}],"nm":"Ellipse 1","np":3,"cix":2,"ix":1,"mn":"ADBE Vector Group","hd":false},{"ty":"gs","o":{"a":0,"k":100,"ix":9},"w":{"a":1,"k":[{"i":{"x":[0.833],"y":[0.833]},"o":{"x":[0.167],"y":[0.167]},"n":["0p833_0p833_0p167_0p167"],"t":0,"s":[5],"e":[10]},{"i":{"x":[0.833],"y":[0.833]},"o":{"x":[0.167],"y":[0.167]},"n":["0p833_0p833_0p167_0p167"],"t":11,"s":[10],"e":[5]},{"t":35}],"ix":10},"g":{"p":3,"k":{"a":0,"k":[0,0,0.627,1,0.5,0.496,0.314,1,1,0.992,0,1],"ix":8}},"s":{"a":0,"k":[0,0],"ix":4},"e":{"a":0,"k":[100,0],"ix":5},"t":1,"lc":1,"lj":1,"ml":4,"nm":"Gradient Stroke 1","mn":"ADBE Vector Graphic - G-Stroke","hd":false}],"ip":0,"op":120,"st":0,"bm":0}]}
"""
private const val lottieClapData = """
    {"v":"5.5.7","meta":{"g":"LottieFiles AE 0.1.20","a":"","k":"","d":"","tc":""},"fr":29.9700012207031,"ip":0,"op":30.0000012219251,"w":240,"h":120,"nm":"spot_celebration","ddd":0,"assets":[],"layers":[{"ddd":0,"ind":1,"ty":4,"nm":"hand-1 Outlines","sr":1,"ks":{"o":{"a":0,"k":100,"ix":11},"r":{"a":0,"k":0,"ix":10},"p":{"a":1,"k":[{"i":{"x":0.833,"y":0.833},"o":{"x":0.389,"y":0},"t":0,"s":[110.657,60.209,0],"to":[0.724,0,0],"ti":[0,0,0]},{"i":{"x":0.623,"y":1},"o":{"x":0.167,"y":0.167},"t":2.024,"s":[115,60.209,0],"to":[0,0,0],"ti":[0.724,0,0]},{"i":{"x":0,"y":0},"o":{"x":0.333,"y":0.333},"t":4.048,"s":[110.657,60.209,0],"to":[0,0,0],"ti":[0,0,0]},{"i":{"x":0.833,"y":0.833},"o":{"x":0.333,"y":0},"t":6.476,"s":[110.657,60.209,0],"to":[0.724,0,0],"ti":[0,0,0]},{"i":{"x":0.665,"y":1},"o":{"x":0.167,"y":0.167},"t":8.5,"s":[115,60.209,0],"to":[0,0,0],"ti":[0.724,0,0]},{"i":{"x":0,"y":0},"o":{"x":0.333,"y":0.333},"t":10.524,"s":[110.657,60.209,0],"to":[0,0,0],"ti":[0,0,0]},{"i":{"x":0.833,"y":0.833},"o":{"x":0.333,"y":0},"t":12.953,"s":[110.657,60.209,0],"to":[0.724,0,0],"ti":[0,0,0]},{"i":{"x":0.673,"y":1},"o":{"x":0.167,"y":0.167},"t":14.976,"s":[115,60.209,0],"to":[0,0,0],"ti":[0.724,0,0]},{"t":17.0000006924242,"s":[110.657,60.209,0]}],"ix":2},"a":{"a":0,"k":[32.553,37.706,0],"ix":1},"s":{"a":0,"k":[100,100,100],"ix":6}},"ao":0,"shapes":[{"ty":"gr","it":[{"ind":0,"ty":"sh","ix":1,"ks":{"a":0,"k":{"i":[[0.429,0.24],[-2.884,5.153],[-0.429,-0.24],[2.885,-5.151]],"o":[[-0.43,-0.24],[2.885,-5.151],[0.429,0.24],[-2.885,5.152]],"v":[[-5.223,9.328],[-0.777,-0.436],[5.223,-9.328],[0.777,0.435]],"c":true},"ix":2},"nm":"Path 1","mn":"ADBE Vector Shape - Group","hd":false},{"ty":"fl","c":{"a":0,"k":[0.9450980392156862,0.7058823529411765,0.47843137254901963,1],"ix":4},"o":{"a":0,"k":100,"ix":5},"r":1,"bm":0,"nm":"Fill 1","mn":"ADBE Vector Graphic - Fill","hd":false},{"ty":"tr","p":{"a":0,"k":[53.708,31.209],"ix":2},"a":{"a":0,"k":[0,0],"ix":1},"s":{"a":0,"k":[100,100],"ix":3},"r":{"a":0,"k":0,"ix":6},"o":{"a":0,"k":100,"ix":7},"sk":{"a":0,"k":0,"ix":4},"sa":{"a":0,"k":0,"ix":5},"nm":"Transform"}],"nm":"Group 1","np":2,"cix":2,"bm":0,"ix":1,"mn":"ADBE Vector Group","hd":false},{"ty":"gr","it":[{"ind":0,"ty":"sh","ix":1,"ks":{"a":0,"k":{"i":[[0.419,0.258],[-4.27,6.936],[-0.419,-0.258],[4.269,-6.935]],"o":[[-0.419,-0.258],[4.269,-6.936],[0.419,0.258],[-4.269,6.936]],"v":[[-7.73,12.558],[-0.758,-0.467],[7.73,-12.558],[0.758,0.467]],"c":true},"ix":2},"nm":"Path 1","mn":"ADBE Vector Shape - Group","hd":false},{"ty":"fl","c":{"a":0,"k":[0.9450980392156862,0.7058823529411765,0.47843137254901963,1],"ix":4},"o":{"a":0,"k":100,"ix":5},"r":1,"bm":0,"nm":"Fill 1","mn":"ADBE Vector Graphic - Fill","hd":false},{"ty":"tr","p":{"a":0,"k":[47.387,21.728],"ix":2},"a":{"a":0,"k":[0,0],"ix":1},"s":{"a":0,"k":[100,100],"ix":3},"r":{"a":0,"k":0,"ix":6},"o":{"a":0,"k":100,"ix":7},"sk":{"a":0,"k":0,"ix":4},"sa":{"a":0,"k":0,"ix":5},"nm":"Transform"}],"nm":"Group 2","np":2,"cix":2,"bm":0,"ix":2,"mn":"ADBE Vector Group","hd":false},{"ty":"gr","it":[{"ind":0,"ty":"sh","ix":1,"ks":{"a":0,"k":{"i":[[0.405,0.279],[-4.573,6.65],[-0.406,-0.278],[4.572,-6.65]],"o":[[-0.405,-0.279],[4.573,-6.65],[0.405,0.279],[-4.573,6.65]],"v":[[-8.28,12.04],[-0.735,-0.506],[8.28,-12.042],[0.734,0.505]],"c":true},"ix":2},"nm":"Path 1","mn":"ADBE Vector Shape - Group","hd":false},{"ty":"fl","c":{"a":0,"k":[0.9450980392156862,0.7058823529411765,0.47843137254901963,1],"ix":4},"o":{"a":0,"k":100,"ix":5},"r":1,"bm":0,"nm":"Fill 1","mn":"ADBE Vector Graphic - Fill","hd":false},{"ty":"tr","p":{"a":0,"k":[37.738,16.357],"ix":2},"a":{"a":0,"k":[0,0],"ix":1},"s":{"a":0,"k":[100,100],"ix":3},"r":{"a":0,"k":0,"ix":6},"o":{"a":0,"k":100,"ix":7},"sk":{"a":0,"k":0,"ix":4},"sa":{"a":0,"k":0,"ix":5},"nm":"Transform"}],"nm":"Group 3","np":2,"cix":2,"bm":0,"ix":3,"mn":"ADBE Vector Group","hd":false},{"ty":"gr","it":[{"ind":0,"ty":"sh","ix":1,"ks":{"a":0,"k":{"i":[[0,0],[-6.325,8.378],[-1.545,0.86],[-1.401,-0.707],[-0.22,-1.281],[-2.303,-1.33],[1.017,-1.762],[-1.904,-1.099],[0.438,-2.609],[0,0],[-1.884,-1.46],[0.706,-2.636],[8.825,-14.156],[10.784,5.694],[-0.367,10.607],[-2.949,2.041],[-0.571,-3.589]],"o":[[0,0],[6.324,-8.378],[1.545,-0.86],[1.402,0.706],[1.596,-1.939],[2.304,1.33],[0,0],[1.904,1.099],[-0.459,2.736],[0,0],[1.945,1.507],[-0.707,2.636],[-9.158,14.689],[-11.407,-6.023],[0.346,-10.016],[2.841,-1.965],[0.691,4.346]],"v":[[-17.535,-7.632],[-6.74,-22.076],[6.333,-36.38],[10.971,-36.749],[13.465,-33.391],[20.363,-34.589],[22.563,-28.538],[26.479,-28.079],[28.655,-22.57],[26.377,-15.826],[30.358,-14.93],[30.987,-7.947],[16.788,21.879],[-17.294,31.762],[-31.936,3.014],[-23.617,-22.577],[-17.156,-17.087]],"c":true},"ix":2},"nm":"Path 1","mn":"ADBE Vector Shape - Group","hd":false},{"ty":"fl","c":{"a":0,"k":[1,0.8392156862745098,0.6862745098039216,1],"ix":4},"o":{"a":0,"k":100,"ix":5},"r":1,"bm":0,"nm":"Fill 1","mn":"ADBE Vector Graphic - Fill","hd":false},{"ty":"tr","p":{"a":0,"k":[32.553,37.706],"ix":2},"a":{"a":0,"k":[0,0],"ix":1},"s":{"a":0,"k":[100,100],"ix":3},"r":{"a":0,"k":0,"ix":6},"o":{"a":0,"k":100,"ix":7},"sk":{"a":0,"k":0,"ix":4},"sa":{"a":0,"k":0,"ix":5},"nm":"Transform"}],"nm":"Group 4","np":2,"cix":2,"bm":0,"ix":4,"mn":"ADBE Vector Group","hd":false}],"ip":0,"op":30.0000012219251,"st":0,"bm":0},{"ddd":0,"ind":2,"ty":4,"nm":"hand-2 Outlines","sr":1,"ks":{"o":{"a":0,"k":100,"ix":11},"r":{"a":0,"k":0,"ix":10},"p":{"a":1,"k":[{"i":{"x":0.833,"y":0.833},"o":{"x":0.389,"y":0},"t":0,"s":[129.637,60.209,0],"to":[-0.94,0,0],"ti":[0,0,0]},{"i":{"x":0.623,"y":1},"o":{"x":0.167,"y":0.167},"t":2.024,"s":[124,60.209,0],"to":[0,0,0],"ti":[-0.94,0,0]},{"i":{"x":0,"y":0},"o":{"x":0.333,"y":0.333},"t":4.048,"s":[129.637,60.209,0],"to":[0,0,0],"ti":[0,0,0]},{"i":{"x":0.833,"y":0.833},"o":{"x":0.333,"y":0},"t":6.476,"s":[129.637,60.209,0],"to":[-0.94,0,0],"ti":[0,0,0]},{"i":{"x":0.665,"y":1},"o":{"x":0.167,"y":0.167},"t":8.5,"s":[124,60.209,0],"to":[0,0,0],"ti":[-0.94,0,0]},{"i":{"x":0,"y":0},"o":{"x":0.333,"y":0.333},"t":10.524,"s":[129.637,60.209,0],"to":[0,0,0],"ti":[0,0,0]},{"i":{"x":0.833,"y":0.833},"o":{"x":0.333,"y":0},"t":12.953,"s":[129.637,60.209,0],"to":[-0.94,0,0],"ti":[0,0,0]},{"i":{"x":0.673,"y":1},"o":{"x":0.167,"y":0.167},"t":14.976,"s":[124,60.209,0],"to":[0,0,0],"ti":[-0.94,0,0]},{"t":17.0000006924242,"s":[129.637,60.209,0]}],"ix":2},"a":{"a":0,"k":[32.553,37.706,0],"ix":1},"s":{"a":0,"k":[100,100,100],"ix":6}},"ao":0,"shapes":[{"ty":"gr","it":[{"ind":0,"ty":"sh","ix":1,"ks":{"a":0,"k":{"i":[[0,0],[-6.324,8.378],[-1.545,0.86],[-1.402,-0.707],[-0.22,-1.281],[-2.303,-1.33],[1.017,-1.762],[-1.905,-1.099],[0.438,-2.609],[0,0],[-1.884,-1.46],[0.707,-2.636],[8.826,-14.156],[10.784,5.694],[-0.366,10.607],[-2.949,2.041],[-0.571,-3.589]],"o":[[0,0],[6.325,-8.378],[1.545,-0.86],[1.401,0.706],[1.596,-1.939],[2.304,1.33],[0,0],[1.904,1.099],[-0.459,2.736],[0,0],[1.945,1.507],[-0.706,2.636],[-9.157,14.689],[-11.407,-6.023],[0.347,-10.016],[2.841,-1.965],[0.692,4.346]],"v":[[-17.535,-7.632],[-6.742,-22.076],[6.333,-36.38],[10.971,-36.749],[13.464,-33.391],[20.363,-34.589],[22.563,-28.538],[26.479,-28.079],[28.655,-22.57],[26.377,-15.826],[30.358,-14.93],[30.986,-7.947],[16.786,21.879],[-17.294,31.762],[-31.938,3.014],[-23.617,-22.577],[-17.158,-17.087]],"c":true},"ix":2},"nm":"Path 1","mn":"ADBE Vector Shape - Group","hd":false},{"ty":"fl","c":{"a":0,"k":[0.9529411764705882,0.7333333333333333,0.5215686274509804,1],"ix":4},"o":{"a":0,"k":100,"ix":5},"r":1,"bm":0,"nm":"Fill 1","mn":"ADBE Vector Graphic - Fill","hd":false},{"ty":"tr","p":{"a":0,"k":[32.553,37.706],"ix":2},"a":{"a":0,"k":[0,0],"ix":1},"s":{"a":0,"k":[100,100],"ix":3},"r":{"a":0,"k":0,"ix":6},"o":{"a":0,"k":100,"ix":7},"sk":{"a":0,"k":0,"ix":4},"sa":{"a":0,"k":0,"ix":5},"nm":"Transform"}],"nm":"Group 1","np":2,"cix":2,"bm":0,"ix":1,"mn":"ADBE Vector Group","hd":false}],"ip":0,"op":30.0000012219251,"st":0,"bm":0},{"ddd":0,"ind":3,"ty":4,"nm":"particle-8 Outlines","sr":1,"ks":{"o":{"a":1,"k":[{"i":{"x":[0.667],"y":[1]},"o":{"x":[0.167],"y":[0.167]},"t":0,"s":[0]},{"i":{"x":[0.833],"y":[0.833]},"o":{"x":[0.333],"y":[0]},"t":6.4,"s":[100]},{"t":22.4000009123707,"s":[0]}],"ix":11},"r":{"a":0,"k":0,"ix":10},"p":{"a":1,"k":[{"i":{"x":0.138,"y":1},"o":{"x":0.167,"y":0.167},"t":0,"s":[161.579,70.143,0],"to":[2.5,0.542,0],"ti":[-2.5,-0.542,0]},{"t":17.0000006924242,"s":[176.579,73.393,0]}],"ix":2},"a":{"a":0,"k":[4.076,4.076,0],"ix":1},"s":{"a":0,"k":[100,100,100],"ix":6}},"ao":0,"shapes":[{"ty":"gr","it":[{"ind":0,"ty":"sh","ix":1,"ks":{"a":0,"k":{"i":[[0.789,-1.677],[-1.677,-0.789],[-0.788,1.678],[1.677,0.789]],"o":[[-0.789,1.677],[1.678,0.789],[0.789,-1.677],[-1.677,-0.788]],"v":[[-3.036,-1.428],[-1.429,3.036],[3.036,1.428],[1.428,-3.038]],"c":true},"ix":2},"nm":"Path 1","mn":"ADBE Vector Shape - Group","hd":false},{"ty":"fl","c":{"a":0,"k":[0.30196078431372547,0.5490196078431373,0.9176470588235294,1],"ix":4},"o":{"a":0,"k":100,"ix":5},"r":1,"bm":0,"nm":"Fill 1","mn":"ADBE Vector Graphic - Fill","hd":false},{"ty":"tr","p":{"a":0,"k":[4.076,4.076],"ix":2},"a":{"a":0,"k":[0,0],"ix":1},"s":{"a":0,"k":[100,100],"ix":3},"r":{"a":0,"k":0,"ix":6},"o":{"a":0,"k":100,"ix":7},"sk":{"a":0,"k":0,"ix":4},"sa":{"a":0,"k":0,"ix":5},"nm":"Transform"}],"nm":"Group 1","np":2,"cix":2,"bm":0,"ix":1,"mn":"ADBE Vector Group","hd":false}],"ip":0,"op":30.0000012219251,"st":0,"bm":0},{"ddd":0,"ind":4,"ty":4,"nm":"particle-7 Outlines","sr":1,"ks":{"o":{"a":1,"k":[{"i":{"x":[0.667],"y":[1]},"o":{"x":[0.333],"y":[0]},"t":6.4,"s":[0]},{"i":{"x":[0.833],"y":[0.833]},"o":{"x":[0.333],"y":[0]},"t":13,"s":[100]},{"t":24.00000097754,"s":[0]}],"ix":11},"r":{"a":0,"k":0,"ix":10},"p":{"a":1,"k":[{"i":{"x":0.212,"y":1},"o":{"x":0.167,"y":0.167},"t":4.8,"s":[173.53,67.136,0],"to":[5.854,-0.5,0],"ti":[-5.854,0.5,0]},{"t":20.0000008146167,"s":[208.655,64.136,0]}],"ix":2},"a":{"a":0,"k":[5.942,5.942,0],"ix":1},"s":{"a":0,"k":[100,100,100],"ix":6}},"ao":0,"shapes":[{"ty":"gr","it":[{"ind":0,"ty":"sh","ix":1,"ks":{"a":0,"k":{"i":[[0.505,-2.865],[-2.864,-0.506],[-0.505,2.865],[2.864,0.505]],"o":[[-0.505,2.865],[2.865,0.505],[0.505,-2.864],[-2.865,-0.505]],"v":[[-5.187,-0.915],[-0.915,5.187],[5.187,0.914],[0.915,-5.187]],"c":true},"ix":2},"nm":"Path 1","mn":"ADBE Vector Shape - Group","hd":false},{"ty":"fl","c":{"a":0,"k":[0.6627450980392157,0.788235294117647,0.9725490196078431,1],"ix":4},"o":{"a":0,"k":100,"ix":5},"r":1,"bm":0,"nm":"Fill 1","mn":"ADBE Vector Graphic - Fill","hd":false},{"ty":"tr","p":{"a":0,"k":[5.942,5.942],"ix":2},"a":{"a":0,"k":[0,0],"ix":1},"s":{"a":0,"k":[100,100],"ix":3},"r":{"a":0,"k":0,"ix":6},"o":{"a":0,"k":100,"ix":7},"sk":{"a":0,"k":0,"ix":4},"sa":{"a":0,"k":0,"ix":5},"nm":"Transform"}],"nm":"Group 1","np":2,"cix":2,"bm":0,"ix":1,"mn":"ADBE Vector Group","hd":false}],"ip":0,"op":30.0000012219251,"st":0,"bm":0},{"ddd":0,"ind":5,"ty":4,"nm":"particle-6 Outlines","sr":1,"ks":{"o":{"a":1,"k":[{"i":{"x":[0.667],"y":[1]},"o":{"x":[0.167],"y":[0.167]},"t":0,"s":[0]},{"i":{"x":[0.833],"y":[0.833]},"o":{"x":[0.333],"y":[0]},"t":6.4,"s":[100]},{"t":22.4000009123707,"s":[0]}],"ix":11},"r":{"a":0,"k":0,"ix":10},"p":{"a":1,"k":[{"i":{"x":0.138,"y":1},"o":{"x":0.167,"y":0.167},"t":0,"s":[168.108,53.605,0],"to":[4.583,-0.875,0],"ti":[-4.583,0.875,0]},{"t":17.0000006924242,"s":[195.608,48.355,0]}],"ix":2},"a":{"a":0,"k":[4.804,4.804,0],"ix":1},"s":{"a":0,"k":[100,100,100],"ix":6}},"ao":0,"shapes":[{"ty":"gr","it":[{"ind":0,"ty":"sh","ix":1,"ks":{"a":0,"k":{"i":[[0.404,-2.292],[-2.292,-0.404],[-0.404,2.292],[2.292,0.404]],"o":[[-0.404,2.292],[2.291,0.404],[0.404,-2.291],[-2.292,-0.404]],"v":[[-4.149,-0.732],[-0.731,4.15],[4.15,0.732],[0.732,-4.15]],"c":true},"ix":2},"nm":"Path 1","mn":"ADBE Vector Shape - Group","hd":false},{"ty":"fl","c":{"a":0,"k":[0.4823529411764706,0.6666666666666666,0.9411764705882353,1],"ix":4},"o":{"a":0,"k":100,"ix":5},"r":1,"bm":0,"nm":"Fill 1","mn":"ADBE Vector Graphic - Fill","hd":false},{"ty":"tr","p":{"a":0,"k":[4.803,4.804],"ix":2},"a":{"a":0,"k":[0,0],"ix":1},"s":{"a":0,"k":[100,100],"ix":3},"r":{"a":0,"k":0,"ix":6},"o":{"a":0,"k":100,"ix":7},"sk":{"a":0,"k":0,"ix":4},"sa":{"a":0,"k":0,"ix":5},"nm":"Transform"}],"nm":"Group 1","np":2,"cix":2,"bm":0,"ix":1,"mn":"ADBE Vector Group","hd":false}],"ip":0,"op":30.0000012219251,"st":0,"bm":0},{"ddd":0,"ind":6,"ty":4,"nm":"particle-5 Outlines","sr":1,"ks":{"o":{"a":1,"k":[{"i":{"x":[0.667],"y":[1]},"o":{"x":[0.333],"y":[0]},"t":6.4,"s":[0]},{"i":{"x":[0.833],"y":[0.833]},"o":{"x":[0.333],"y":[0]},"t":13,"s":[100]},{"t":24.00000097754,"s":[0]}],"ix":11},"r":{"a":0,"k":0,"ix":10},"p":{"a":1,"k":[{"i":{"x":0.212,"y":1},"o":{"x":0.167,"y":0.167},"t":4.8,"s":[163.17,58.428,0],"to":[2.771,-0.562,0],"ti":[-2.771,0.562,0]},{"t":20.0000008146167,"s":[179.795,55.053,0]}],"ix":2},"a":{"a":0,"k":[2.982,2.982,0],"ix":1},"s":{"a":0,"k":[100,100,100],"ix":6}},"ao":0,"shapes":[{"ty":"gr","it":[{"ind":0,"ty":"sh","ix":1,"ks":{"a":0,"k":{"i":[[0.242,-1.375],[-1.375,-0.242],[-0.242,1.375],[1.375,0.243]],"o":[[-0.243,1.375],[1.375,0.243],[0.243,-1.375],[-1.376,-0.242]],"v":[[-2.49,-0.439],[-0.439,2.489],[2.49,0.439],[0.439,-2.49]],"c":true},"ix":2},"nm":"Path 1","mn":"ADBE Vector Shape - Group","hd":false},{"ty":"fl","c":{"a":0,"k":[0.796078431372549,0.8784313725490196,1,1],"ix":4},"o":{"a":0,"k":100,"ix":5},"r":1,"bm":0,"nm":"Fill 1","mn":"ADBE Vector Graphic - Fill","hd":false},{"ty":"tr","p":{"a":0,"k":[2.982,2.982],"ix":2},"a":{"a":0,"k":[0,0],"ix":1},"s":{"a":0,"k":[100,100],"ix":3},"r":{"a":0,"k":0,"ix":6},"o":{"a":0,"k":100,"ix":7},"sk":{"a":0,"k":0,"ix":4},"sa":{"a":0,"k":0,"ix":5},"nm":"Transform"}],"nm":"Group 1","np":2,"cix":2,"bm":0,"ix":1,"mn":"ADBE Vector Group","hd":false}],"ip":0,"op":30.0000012219251,"st":0,"bm":0},{"ddd":0,"ind":7,"ty":4,"nm":"particle-4 Outlines","sr":1,"ks":{"o":{"a":1,"k":[{"i":{"x":[0.667],"y":[1]},"o":{"x":[0.167],"y":[0.167]},"t":0,"s":[0]},{"i":{"x":[0.833],"y":[0.833]},"o":{"x":[0.333],"y":[0]},"t":6.4,"s":[100]},{"t":22.4000009123707,"s":[0]}],"ix":11},"r":{"a":0,"k":0,"ix":10},"p":{"a":1,"k":[{"i":{"x":0.138,"y":1},"o":{"x":0.167,"y":0.167},"t":0,"s":[73.269,64.839,0],"to":[-4.458,0.625,0],"ti":[4.458,-0.625,0]},{"t":17.0000006924242,"s":[46.519,68.589,0]}],"ix":2},"a":{"a":0,"k":[3.164,3.163,0],"ix":1},"s":{"a":0,"k":[100,100,100],"ix":6}},"ao":0,"shapes":[{"ty":"gr","it":[{"ind":0,"ty":"sh","ix":1,"ks":{"a":0,"k":{"i":[[0.601,-1.278],[-1.277,-0.601],[-0.601,1.277],[1.277,0.6]],"o":[[-0.601,1.277],[1.278,0.6],[0.601,-1.278],[-1.278,-0.601]],"v":[[-2.313,-1.088],[-1.088,2.313],[2.313,1.089],[1.088,-2.312]],"c":true},"ix":2},"nm":"Path 1","mn":"ADBE Vector Shape - Group","hd":false},{"ty":"fl","c":{"a":0,"k":[0.796078431372549,0.8784313725490196,1,1],"ix":4},"o":{"a":0,"k":100,"ix":5},"r":1,"bm":0,"nm":"Fill 1","mn":"ADBE Vector Graphic - Fill","hd":false},{"ty":"tr","p":{"a":0,"k":[3.164,3.164],"ix":2},"a":{"a":0,"k":[0,0],"ix":1},"s":{"a":0,"k":[100,100],"ix":3},"r":{"a":0,"k":0,"ix":6},"o":{"a":0,"k":100,"ix":7},"sk":{"a":0,"k":0,"ix":4},"sa":{"a":0,"k":0,"ix":5},"nm":"Transform"}],"nm":"Group 1","np":2,"cix":2,"bm":0,"ix":1,"mn":"ADBE Vector Group","hd":false}],"ip":0,"op":30.0000012219251,"st":0,"bm":0},{"ddd":0,"ind":8,"ty":4,"nm":"particle-3 Outlines","sr":1,"ks":{"o":{"a":1,"k":[{"i":{"x":[0.667],"y":[1]},"o":{"x":[0.333],"y":[0]},"t":6.4,"s":[0]},{"i":{"x":[0.833],"y":[0.833]},"o":{"x":[0.333],"y":[0]},"t":13,"s":[100]},{"t":24.00000097754,"s":[0]}],"ix":11},"r":{"a":0,"k":0,"ix":10},"p":{"a":1,"k":[{"i":{"x":0.212,"y":1},"o":{"x":0.167,"y":0.167},"t":4.8,"s":[75.582,55.3,0],"to":[-2.375,-0.333,0],"ti":[2.375,0.333,0]},{"t":20.0000008146167,"s":[61.332,53.3,0]}],"ix":2},"a":{"a":0,"k":[4.016,4.015,0],"ix":1},"s":{"a":0,"k":[100,100,100],"ix":6}},"ao":0,"shapes":[{"ty":"gr","it":[{"ind":0,"ty":"sh","ix":1,"ks":{"a":0,"k":{"i":[[1.497,1.055],[1.055,-1.497],[-1.497,-1.056],[-1.055,1.497]],"o":[[-1.497,-1.055],[-1.056,1.497],[1.497,1.055],[1.056,-1.496]],"v":[[1.911,-2.711],[-2.71,-1.911],[-1.911,2.71],[2.71,1.91]],"c":true},"ix":2},"nm":"Path 1","mn":"ADBE Vector Shape - Group","hd":false},{"ty":"fl","c":{"a":0,"k":[0.6627450980392157,0.788235294117647,0.9725490196078431,1],"ix":4},"o":{"a":0,"k":100,"ix":5},"r":1,"bm":0,"nm":"Fill 1","mn":"ADBE Vector Graphic - Fill","hd":false},{"ty":"tr","p":{"a":0,"k":[4.016,4.015],"ix":2},"a":{"a":0,"k":[0,0],"ix":1},"s":{"a":0,"k":[100,100],"ix":3},"r":{"a":0,"k":0,"ix":6},"o":{"a":0,"k":100,"ix":7},"sk":{"a":0,"k":0,"ix":4},"sa":{"a":0,"k":0,"ix":5},"nm":"Transform"}],"nm":"Group 1","np":2,"cix":2,"bm":0,"ix":1,"mn":"ADBE Vector Group","hd":false}],"ip":0,"op":30.0000012219251,"st":0,"bm":0},{"ddd":0,"ind":9,"ty":4,"nm":"particle-2 Outlines","sr":1,"ks":{"o":{"a":1,"k":[{"i":{"x":[0.667],"y":[1]},"o":{"x":[0.167],"y":[0.167]},"t":0,"s":[0]},{"i":{"x":[0.833],"y":[0.833]},"o":{"x":[0.333],"y":[0]},"t":6.4,"s":[100]},{"t":22.4000009123707,"s":[0]}],"ix":11},"r":{"a":0,"k":0,"ix":10},"p":{"a":1,"k":[{"i":{"x":0.138,"y":1},"o":{"x":0.167,"y":0.167},"t":0,"s":[73.293,48.233,0],"to":[-3.708,-0.958,0],"ti":[3.708,0.958,0]},{"t":17.0000006924242,"s":[51.043,42.483,0]}],"ix":2},"a":{"a":0,"k":[5.368,5.368,0],"ix":1},"s":{"a":0,"k":[100,100,100],"ix":6}},"ao":0,"shapes":[{"ty":"gr","it":[{"ind":0,"ty":"sh","ix":1,"ks":{"a":0,"k":{"i":[[2.197,-1.138],[1.138,2.197],[-2.198,1.139],[-1.138,-2.198]],"o":[[-2.197,1.139],[-1.139,-2.198],[2.197,-1.138],[1.139,2.197]],"v":[[2.062,3.979],[-3.979,2.061],[-2.062,-3.98],[3.979,-2.062]],"c":true},"ix":2},"nm":"Path 1","mn":"ADBE Vector Shape - Group","hd":false},{"ty":"fl","c":{"a":0,"k":[0.4823529411764706,0.6666666666666666,0.9411764705882353,1],"ix":4},"o":{"a":0,"k":100,"ix":5},"r":1,"bm":0,"nm":"Fill 1","mn":"ADBE Vector Graphic - Fill","hd":false},{"ty":"tr","p":{"a":0,"k":[5.368,5.368],"ix":2},"a":{"a":0,"k":[0,0],"ix":1},"s":{"a":0,"k":[100,100],"ix":3},"r":{"a":0,"k":0,"ix":6},"o":{"a":0,"k":100,"ix":7},"sk":{"a":0,"k":0,"ix":4},"sa":{"a":0,"k":0,"ix":5},"nm":"Transform"}],"nm":"Group 1","np":2,"cix":2,"bm":0,"ix":1,"mn":"ADBE Vector Group","hd":false}],"ip":0,"op":30.0000012219251,"st":0,"bm":0},{"ddd":0,"ind":10,"ty":4,"nm":"particle-1 Outlines","sr":1,"ks":{"o":{"a":1,"k":[{"i":{"x":[0.667],"y":[1]},"o":{"x":[0.333],"y":[0]},"t":6.4,"s":[0]},{"i":{"x":[0.833],"y":[0.833]},"o":{"x":[0.333],"y":[0]},"t":13,"s":[100]},{"t":24.00000097754,"s":[0]}],"ix":11},"r":{"a":0,"k":0,"ix":10},"p":{"a":1,"k":[{"i":{"x":0.212,"y":1},"o":{"x":0.167,"y":0.167},"t":4.8,"s":[62.37,58.549,0],"to":[-5.25,-0.25,0],"ti":[5.25,0.25,0]},{"t":20.0000008146167,"s":[30.87,57.049,0]}],"ix":2},"a":{"a":0,"k":[4.349,4.348,0],"ix":1},"s":{"a":0,"k":[100,100,100],"ix":6}},"ao":0,"shapes":[{"ty":"gr","it":[{"ind":0,"ty":"sh","ix":1,"ks":{"a":0,"k":{"i":[[0.364,-2.063],[-2.062,-0.364],[-0.364,2.062],[2.063,0.364]],"o":[[-0.364,2.063],[2.063,0.364],[0.364,-2.063],[-2.062,-0.364]],"v":[[-3.734,-0.659],[-0.659,3.734],[3.734,0.658],[0.659,-3.734]],"c":true},"ix":2},"nm":"Path 1","mn":"ADBE Vector Shape - Group","hd":false},{"ty":"fl","c":{"a":0,"k":[0.30196078431372547,0.5490196078431373,0.9176470588235294,1],"ix":4},"o":{"a":0,"k":100,"ix":5},"r":1,"bm":0,"nm":"Fill 1","mn":"ADBE Vector Graphic - Fill","hd":false},{"ty":"tr","p":{"a":0,"k":[4.348,4.348],"ix":2},"a":{"a":0,"k":[0,0],"ix":1},"s":{"a":0,"k":[100,100],"ix":3},"r":{"a":0,"k":0,"ix":6},"o":{"a":0,"k":100,"ix":7},"sk":{"a":0,"k":0,"ix":4},"sa":{"a":0,"k":0,"ix":5},"nm":"Transform"}],"nm":"Group 1","np":2,"cix":2,"bm":0,"ix":1,"mn":"ADBE Vector Group","hd":false}],"ip":0,"op":30.0000012219251,"st":0,"bm":0}],"markers":[]}
"""

private const val lottieClapUrl = "https://assets10.lottiefiles.com/packages/lf20_dflqwg4a.json"

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun App() {

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {

        val compositionJson by rememberLottieComposition(LottieCompositionSpec.JsonString(lottieClapData))
        val compositionUrl by rememberLottieComposition(LottieCompositionSpec.Url(lottieClapUrl))

        val progress = animateLottieCompositionAsState(
            composition = compositionJson,
            iterations = LottieConstants.IterateForever,
        )

        val painter = rememberLottiePainter(compositionJson, progress.value)
        val painterUrl = rememberLottiePainter(compositionUrl, progress.value)

        FlowRow {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painter,
                    contentDescription = null,
                )
                Text("Image with LottiePainter")
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LottieAnimation(
                    composition = compositionJson,
                    progress = { progress.value },
                )
                Text("LottieAnimation")
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterUrl,
                    contentDescription = null,
                )
                Text("Image with LottiePainter from url")
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LottieAnimation(
                    composition = compositionUrl,
                    progress = { progress.value },
                )
                Text("LottieAnimation from url")
            }
        }
    }
}
