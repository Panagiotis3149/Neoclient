package neo.util.render.animation;

public class AnimationEngine {

    private long prevTime;

    private float animationValue;

    private float startValue;

    private float endValue;

    private boolean isIncreasing;

    private float changeValuePms;

    private boolean isDrawAnimation = false;

    private boolean resetUsingBackWardsAnimation = false;


    public AnimationEngine(float startValue, float endValue, long time) {
        this.prevTime = System.currentTimeMillis();
        this.startValue = startValue;
        this.endValue = (startValue == endValue ? endValue+1 : endValue);
        this.animationValue = startValue;
        this.isIncreasing = endValue > startValue;
        float animationDistance = Math.abs(startValue - endValue);
        this.changeValuePms = animationDistance / time;


    }

    public AnimationEngine(float startValue, float endValue, long time, boolean instaIsDrawAnimation) {
        this.prevTime = System.currentTimeMillis();
        this.startValue = startValue;
        this.endValue = (startValue == endValue ? endValue+1 : endValue);
        this.animationValue = startValue;
        this.isIncreasing = endValue > startValue;
        float animationDistance = Math.abs(startValue - endValue);
        this.changeValuePms = animationDistance / time;

        this.isDrawAnimation = instaIsDrawAnimation;

    }

    public AnimationEngine() {
    }


    public float getAnimationValue() {
        updateAnimationValue();
        return animationValue;
    }

    public boolean isAnimationDone() {
        return animationValue == endValue;
    }


    private void updateAnimationValue() {
        if(isDrawAnimation) {
            resetUsingBackWardsAnimation = false;
            if (animationValue == endValue) return;

            if (isIncreasing) {
                if (animationValue >= endValue) {
                    animationValue = endValue;
                    return;
                }


                animationValue += (changeValuePms) * (System.currentTimeMillis() - prevTime);
                //animationValue += resetUsingBackWardsAnimation ? -1 *((changeValuePms) * (System.currentTimeMillis() - prevTime)) : ((changeValuePms) * (System.currentTimeMillis() - prevTime));

                if (animationValue > endValue)
                    animationValue = endValue;
                this.prevTime = System.currentTimeMillis();
            } else {
                if (animationValue <= endValue) {
                    animationValue = endValue;
                    return;
                }
                animationValue -= (changeValuePms) * (System.currentTimeMillis() - prevTime);

                //animationValue -= resetUsingBackWardsAnimation ? -1 *((changeValuePms) * (System.currentTimeMillis() - prevTime)) : ((changeValuePms) * (System.currentTimeMillis() - prevTime));

                if (animationValue < endValue)
                    animationValue = endValue;
                this.prevTime = System.currentTimeMillis();
            }
        }
        else if (resetUsingBackWardsAnimation) {
            setIsDrawAnimation(false);
            if (animationValue == startValue) {
                reset();
                resetUsingBackWardsAnimation = false;
                return;
            }
            if(isIncreasing) {
                if(animationValue <= startValue) {
                    reset();
                    return;
                }
            }
            animationValue -= (changeValuePms) * (System.currentTimeMillis() - prevTime);
            if (animationValue < startValue)
                reset();
            this.prevTime = System.currentTimeMillis();

        }

    }



    public void reset() {
        animationValue = startValue;
        prevTime = System.currentTimeMillis();
    }



    public void AnimationUpdateValue(float startValue, float endValue, long time) {
        reset();
        this.prevTime = System.currentTimeMillis();
        this.startValue = startValue;
        this.endValue = (startValue == endValue ? endValue+1 : endValue);
        this.animationValue = startValue;
        this.isIncreasing = endValue > startValue;
        float animationDistance = Math.abs(startValue - endValue);
        this.changeValuePms = animationDistance / time;
    }

    public void AnimationUpdateValue(float startValue, float endValue, long time, boolean instaDrawAnimation) {
        this.prevTime = System.currentTimeMillis();
        this.startValue = startValue;
        this.endValue = (startValue == endValue ? endValue+1 : endValue);
        this.animationValue = startValue;
        this.isIncreasing = endValue > startValue;
        float animationDistance = Math.abs(startValue - endValue);
        this.changeValuePms = animationDistance / time;

        this.isDrawAnimation = instaDrawAnimation;

    }


    public void setIsDrawAnimation(boolean drawAnimation) {
        this.isDrawAnimation = drawAnimation;

    }

    public boolean getIsDrawAnimation() {
        return isDrawAnimation;

    }

    public void resetUsingBackWardsAnimation() {
        prevTime = System.currentTimeMillis();
        setIsDrawAnimation(false);
        this.resetUsingBackWardsAnimation = true;

    }

}