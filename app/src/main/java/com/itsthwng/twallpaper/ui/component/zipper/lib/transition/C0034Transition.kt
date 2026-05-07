package com.itsthwng.twallpaper.ui.component.zipper.lib.transition

import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

object C0034Transition {
    fun easeBounceOut(d: Double, d2: Double, d3: Double, d4: Double): Double {
        val d5: Double
        val d6: Double
        val d7: Double
        val d8 = d / d4
        if (d8 < 0.36363636363636365) {
            d7 = 7.5625 * d8 * d8
        } else {
            if (d8 < 0.7272727272727273) {
                val d9 = d8 - 0.5454545454545454
                d5 = 7.5625 * d9 * d9
                d6 = 0.75
            } else if (d8 < 0.9090909090909091) {
                val d10 = d8 - 0.8181818181818182
                d5 = 7.5625 * d10 * d10
                d6 = 0.9375
            } else {
                val d11 = d8 - 0.9545454545454546
                d5 = 7.5625 * d11 * d11
                d6 = 0.984375
            }
            d7 = d5 + d6
        }
        return (d3 * d7) + d2
    }

    fun easeInCubic(d: Double, d2: Double, d3: Double, d4: Double): Double {
        val d5 = d / d4
        return (d3 * d5 * d5 * d5) + d2
    }

    fun easeInOutCubic(d: Double, d2: Double, d3: Double, d4: Double): Double {
        val d5: Double
        val d6 = d / (d4 / 2.0)
        if (d6 < 1.0) {
            d5 = (d3 / 2.0) * d6 * d6 * d6
        } else {
            val d7 = d6 - 2.0
            d5 = (d3 / 2.0) * ((d7 * d7 * d7) + 2.0)
        }
        return d5 + d2
    }

    fun easeInOutQuad(d: Double, d2: Double, d3: Double, d4: Double): Double {
        val d5: Double
        var d6 = d / (d4 / 2.0)
        if (d6 < 1.0) {
            d5 = (d3 / 2.0) * d6
        } else {
            val d7 = d6 - 1.0
            d5 = (-d3) / 2.0
            d6 = (d7 * (d7 - 2.0)) - 1.0
        }
        return (d5 * d6) + d2
    }

    fun easeInOutQuart(d: Double, d2: Double, d3: Double, d4: Double): Double {
        val d5: Double
        val d6 = d / (d4 / 2.0)
        if (d6 < 1.0) {
            d5 = (d3 / 2.0) * d6 * d6 * d6 * d6
        } else {
            val d7 = d6 - 2.0
            d5 = ((-d3) / 2.0) * ((((d7 * d7) * d7) * d7) - 2.0)
        }
        return d5 + d2
    }

    fun easeInOutQuint(d: Double, d2: Double, d3: Double, d4: Double): Double {
        val d5: Double
        val d6 = d / (d4 / 2.0)
        if (d6 < 1.0) {
            d5 = (d3 / 2.0) * d6 * d6 * d6 * d6 * d6
        } else {
            val d7 = d6 - 2.0
            d5 = (d3 / 2.0) * ((d7 * d7 * d7 * d7 * d7) + 2.0)
        }
        return d5 + d2
    }

    fun easeInQuad(d: Double, d2: Double, d3: Double, d4: Double): Double {
        val d5 = d / d4
        return (d3 * d5 * d5) + d2
    }

    fun easeInQuart(d: Double, d2: Double, d3: Double, d4: Double): Double {
        val d5 = d / d4
        return (d3 * d5 * d5 * d5 * d5) + d2
    }


    fun easeInQuint(d: Double, d2: Double, d3: Double, d4: Double): Double {
        val d5 = d / d4
        return (d3 * d5 * d5 * d5 * d5 * d5) + d2
    }

    fun easeOutCubic(d: Double, d2: Double, d3: Double, d4: Double): Double {
        val d5 = (d / d4) - 1.0
        return (d3 * ((d5 * d5 * d5) + 1.0)) + d2
    }

    fun easeOutQuad(d: Double, d2: Double, d3: Double, d4: Double): Double {
        val d5 = d / d4
        return ((-d3) * d5 * (d5 - 2.0)) + d2
    }

    fun easeOutQuart(d: Double, d2: Double, d3: Double, d4: Double): Double {
        val d5 = (d / d4) - 1.0
        return ((-d3) * ((((d5 * d5) * d5) * d5) - 1.0)) + d2
    }

    fun easeOutQuint(d: Double, d2: Double, d3: Double, d4: Double): Double {
        val d5 = (d / d4) - 1.0
        return (d3 * ((d5 * d5 * d5 * d5 * d5) + 1.0)) + d2
    }

    fun linearTween(d: Double, d2: Double, d3: Double, d4: Double): Double {
        return ((d3 * d) / d4) + d2
    }

    @JvmStatic
    fun getValue(
        transitionType: TransitionType,
        d: Double,
        d2: Double,
        d3: Double,
        d4: Double
    ): Double {
        if (transitionType == TransitionType.EaseInCirc) {
            return easeInCirc(d, d2, d3 - d2, d4)
        }
        if (transitionType == TransitionType.EaseInCubic) {
            return easeInCubic(d, d2, d3 - d2, d4)
        }
        if (transitionType == TransitionType.EaseInExpo) {
            return easeInExpo(d, d2, d3 - d2, d4)
        }
        if (transitionType == TransitionType.EaseInOutCirc) {
            return easeInOutCirc(d, d2, d3 - d2, d4)
        }
        if (transitionType == TransitionType.EaseInOutCubic) {
            return easeInOutCubic(d, d2, d3 - d2, d4)
        }
        if (transitionType == TransitionType.EaseInOutExpo) {
            return easeInOutExpo(d, d2, d3 - d2, d4)
        }
        if (transitionType == TransitionType.EaseInOutQuad) {
            return easeInOutQuad(d, d2, d3 - d2, d4)
        }
        if (transitionType == TransitionType.EaseInOutQuart) {
            return easeInOutQuart(d, d2, d3 - d2, d4)
        }
        if (transitionType == TransitionType.EaseInOutQuint) {
            return easeInOutQuint(d, d2, d3 - d2, d4)
        }
        if (transitionType == TransitionType.EaseInOutSine) {
            return easeInOutSine(d, d2, d3 - d2, d4)
        }
        if (transitionType == TransitionType.EaseInQuad) {
            return easeInQuad(d, d2, d3 - d2, d4)
        }
        if (transitionType == TransitionType.EaseInQuart) {
            return easeInQuart(d, d2, d3 - d2, d4)
        }
        if (transitionType == TransitionType.EaseInQuint) {
            return easeInQuint(d, d2, d3 - d2, d4)
        }
        if (transitionType == TransitionType.EaseInSine) {
            return easeInSine(d, d2, d3 - d2, d4)
        }
        if (transitionType == TransitionType.EaseOutCirc) {
            return easeOutCirc(d, d2, d3 - d2, d4)
        }
        if (transitionType == TransitionType.EaseOutCubic) {
            return easeOutCubic(d, d2, d3 - d2, d4)
        }
        if (transitionType == TransitionType.EaseOutExpo) {
            return easeOutExpo(d, d2, d3 - d2, d4)
        }
        if (transitionType == TransitionType.EaseOutQuad) {
            return easeOutQuad(d, d2, d3 - d2, d4)
        }
        if (transitionType == TransitionType.EaseOutQuart) {
            return easeOutQuart(d, d2, d3 - d2, d4)
        }
        if (transitionType == TransitionType.EaseOutQuint) {
            return easeOutQuint(d, d2, d3 - d2, d4)
        }
        if (transitionType == TransitionType.EaseOutSine) {
            return easeOutSine(d, d2, d3 - d2, d4)
        }
        if (transitionType == TransitionType.LinearTween) {
            return linearTween(d, d2, d3 - d2, d4)
        }
        if (transitionType == TransitionType.Special1) {
            return easeInOutQuad(d, d2, d3 - d2, d4)
        }
        if (transitionType == TransitionType.Special2) {
            return easeInOutQuart(d, d2, d3 - d2, d4)
        }
        if (transitionType == TransitionType.Special3) {
            return easeOutCubic(d, d2, d3 - d2, d4)
        }
        if (transitionType == TransitionType.Special4) {
            return easeOutSine(d, d2, d3 - d2, d4)
        }
        if (transitionType == TransitionType.EaseInBounce) {
            return easeBounceIn(d, d2, d3 - d2, d4)
        }
        if (transitionType == TransitionType.EaseOutBounce) {
            return easeBounceOut(d, d2, d3 - d2, d4)
        }
        return easeInOutSine(d, d2, d3 - d2, d4)
    }

    fun easeInSine(d: Double, d2: Double, d3: Double, d4: Double): Double {
        return ((-d3) * cos((d / d4) * 1.5707963267948966)) + d3 + d2
    }

    fun easeOutSine(d: Double, d2: Double, d3: Double, d4: Double): Double {
        return (d3 * sin((d / d4) * 1.5707963267948966)) + d2
    }

    fun easeInOutSine(d: Double, d2: Double, d3: Double, d4: Double): Double {
        return (((-d3) / 2.0) * (cos((d * 3.141592653589793) / d4) - 1.0)) + d2
    }

    fun easeInExpo(d: Double, d2: Double, d3: Double, d4: Double): Double {
        return (d3 * 2.0.pow(((d / d4) - 1.0) * 10.0)) + d2
    }

    fun easeOutExpo(d: Double, d2: Double, d3: Double, d4: Double): Double {
        return (d3 * (((-2.0).pow((d * (-10.0)) / d4)) + 1.0)) + d2
    }

    fun easeInOutExpo(d: Double, d2: Double, d3: Double, d4: Double): Double {
        val d5: Double
        val d6: Double
        val d7 = d / (d4 / 2.0)
        if (d7 < 1.0) {
            d5 = d3 / 2.0
            d6 = 2.0.pow((d7 - 1.0) * 10.0)
        } else {
            d5 = d3 / 2.0
            d6 = ((-2.0).pow((d7 - 1.0) * (-10.0))) + 2.0
        }
        return (d5 * d6) + d2
    }

    fun easeInCirc(d: Double, d2: Double, d3: Double, d4: Double): Double {
        val d5 = d / d4
        return ((-d3) * (sqrt(1.0 - (d5 * d5)) - 1.0)) + d2
    }

    fun easeOutCirc(d: Double, d2: Double, d3: Double, d4: Double): Double {
        val d5 = (d / d4) - 1.0
        return (d3 * sqrt(1.0 - (d5 * d5))) + d2
    }

    fun easeInOutCirc(d: Double, d2: Double, d3: Double, d4: Double): Double {
        val d5: Double
        val sqrt: Double
        val d6 = d / (d4 / 2.0)
        if (d6 < 1.0) {
            d5 = (-d3) / 2.0
            sqrt = sqrt(1.0 - (d6 * d6)) - 1.0
        } else {
            val d7 = d6 - 2.0
            d5 = d3 / 2.0
            sqrt = sqrt(1.0 - (d7 * d7)) + 1.0
        }
        return (d5 * sqrt) + d2
    }

    fun easeBounceIn(d: Double, d2: Double, d3: Double, d4: Double): Double {
        return (d3 - easeBounceOut(d4 - d, 0.0, d3, d4)) + d2
    }
}
