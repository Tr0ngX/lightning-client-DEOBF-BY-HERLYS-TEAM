#version 330

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

layout(std140) uniform MenuBgConfig {
    vec4 Params; // x: Time, y: Alpha, z: unused, w: unused
};

in vec2 texCoord;
out vec4 fragColor;

// Lightning - simple purple galaxy background

float hash21(vec2 p) {
    vec3 p3 = fract(vec3(p.xyx) * 0.1031);
    p3 += dot(p3, p3.yzx + 33.33);
    return fract((p3.x + p3.y) * p3.z);
}

float vnoise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    vec2 u = f * f * (3.0 - 2.0 * f);
    return mix(mix(hash21(i), hash21(i + vec2(1.0, 0.0)), u.x),
               mix(hash21(i + vec2(0.0, 1.0)), hash21(i + vec2(1.0, 1.0)), u.x), u.y);
}

float fbm(vec2 p) {
    float v = 0.0;
    float a = 0.5;
    for (int i = 0; i < 5; i++) {
        v += a * vnoise(p);
        p = p * 2.03 + vec2(7.1, 3.7);
        a *= 0.5;
    }
    return v;
}

// one layer of procedural stars: returns rgb
vec3 starLayer(vec2 uv, float scale, float density, float t, float seed) {
    vec2 g = uv * scale;
    vec2 id = floor(g);
    vec2 f = fract(g) - 0.5;

    float h = hash21(id + seed);
    float present = step(h, density);

    vec2 off = vec2(hash21(id + seed + 11.3), hash21(id + seed + 27.7)) - 0.5;
    vec2 p = f - off * 0.5;
    float d = length(p);

    float sz = mix(0.025, 0.075, hash21(id + seed + 5.1));
    float tw = 0.65 + 0.35 * sin(t * (0.8 + 2.5 * hash21(id + seed + 9.9)) + h * 60.0);

    float core = exp(-(d * d) / (sz * sz * 0.35));
    float halo = exp(-d / (sz * 2.0)) * 0.16;
    // fade to zero at the cell border so no square edges appear
    float win = smoothstep(0.0, 0.30, 0.5 - max(abs(f.x), abs(f.y)));

    float k = hash21(id + seed + 3.3);
    vec3 tint = mix(vec3(0.75, 0.85, 1.00), vec3(1.00, 0.80, 1.00), k);
    tint = mix(tint, vec3(0.80, 0.65, 1.00), step(0.8, k));

    return tint * (core + halo) * win * tw * present;
}


float hash11(float p) {
    p = fract(p * 0.1031);
    p *= p + 33.33;
    p *= p + p;
    return fract(p);
}

// Lightning: returns (bolt intensity, sky flash intensity)
vec2 lightningFx(vec2 uv, float t) {
    const float CYCLE = 6.0;
    float id = floor(t / CYCLE);
    float lt = t - id * CYCLE;

    float start = 0.6 + hash11(id * 7.31) * 3.5;
    float dt1 = lt - start;
    float dt2 = lt - start - 0.16;
    float env = 0.0;
    if (dt1 > 0.0) env += exp(-dt1 * 7.0);
    if (dt2 > 0.0) env += 0.7 * exp(-dt2 * 9.0);
    env = min(env, 1.0);
    // brief rapid flicker while the strike is fresh
    env *= 0.75 + 0.25 * step(0.5, fract(lt * 24.0));
    if (env < 0.003) return vec2(0.0);

    float bx = (hash11(id * 3.17 + 1.0) - 0.5) * 1.3;
    float y = uv.y;

    float disp = (vnoise(vec2(y * 3.0, id * 5.3)) - 0.5) * 0.40
               + (vnoise(vec2(y * 9.0, id * 2.9 + 7.0)) - 0.5) * 0.14
               + (vnoise(vec2(y * 22.0, id * 1.7 + 3.0)) - 0.5) * 0.04;
    float x0 = bx + disp;

    float d = abs(uv.x - x0);
    float endY = -0.25 + hash11(id * 9.7) * 0.25;
    float mask = (1.0 - smoothstep(0.50, 0.58, y)) * smoothstep(endY - 0.10, endY + 0.08, y);

    float bolt = exp(-d * 260.0) + 0.30 * exp(-d * 38.0);
    bolt *= mask;

    // one side branch
    float yb = 0.20 - hash11(id * 4.1) * 0.25;
    float side = hash11(id * 6.6) > 0.5 ? 1.0 : -1.0;
    float xb = bx + (vnoise(vec2(yb * 3.0, id * 5.3)) - 0.5) * 0.40
             + (vnoise(vec2(yb * 9.0, id * 2.9 + 7.0)) - 0.5) * 0.14;
    float by = yb - y;
    float x1 = xb + side * by * 0.55
             + (vnoise(vec2(y * 14.0, id * 8.1)) - 0.5) * 0.06;
    float db = abs(uv.x - x1);
    float bmask = step(0.0, by) * (1.0 - smoothstep(0.05, 0.30, by));
    bolt += (exp(-db * 320.0) + 0.20 * exp(-db * 45.0)) * bmask * 0.7;

    float sky = env * 0.9;
    return vec2(bolt * env, sky);
}

void main() {
    vec2 uv = (gl_FragCoord.xy - 0.5 * OutSize) / OutSize.y;
    float t = Params.x;

    // deep purple base fading to near black at the edges
    float r = length(uv * vec2(0.8, 1.0));
    vec3 col = mix(vec3(0.035, 0.016, 0.075), vec3(0.006, 0.003, 0.020), smoothstep(0.0, 1.1, r));

    // soft diagonal galaxy band
    vec2 dir = normalize(vec2(0.82, 0.57));
    float across = dot(uv, vec2(-dir.y, dir.x));
    float band = exp(-across * across / 0.10);

    // one soft nebula layer, drifting very slowly
    vec2 p = uv * 1.3 + vec2(t * 0.010, t * 0.006);
    vec2 q = vec2(fbm(p), fbm(p + vec2(5.2, 1.3)));
    float n = fbm(p + 1.6 * q);
    col += vec3(0.26, 0.10, 0.50) * smoothstep(0.35, 0.90, n) * (band * 0.8 + 0.25) * 1.35;

    // sparse stars
    vec3 st = vec3(0.0);
    st += starLayer(uv + vec2(t * 0.0020, 0.0), 30.0, 0.30, t, 1.0) * 0.55;
    st += starLayer(uv + vec2(t * 0.0045, 0.0), 13.0, 0.22, t, 19.0) * 0.90;
    st += starLayer(uv + vec2(t * 0.0075, 0.0), 6.0,  0.18, t, 41.0) * 1.20;
    col += st * (0.7 + band * 0.6);

    // keep the menu area calm so text stays readable
    vec2 c = uv * vec2(1.0, 1.35);
    col *= 1.0 - exp(-dot(c, c) / 0.10) * 0.30;

    // vignette
    float vig = 1.0 - smoothstep(0.50, 1.15, length(uv * vec2(0.9, 1.15)));
    col *= mix(0.45, 1.0, vig);

    // thunder & lightning
    vec2 lf = lightningFx(uv, t);
    col += vec3(0.20, 0.12, 0.38) * lf.y * (0.35 + 0.65 * smoothstep(-0.5, 0.5, uv.y));
    col += vec3(0.80, 0.75, 1.00) * lf.x * 1.4;

    // film grain
    col += (hash21(gl_FragCoord.xy + fract(t)) - 0.5) * 0.010;

    fragColor = vec4(clamp(col, 0.0, 1.0), Params.y);
}
