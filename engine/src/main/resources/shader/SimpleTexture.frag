// 16.10.24: Shader refactored to provide the following effects:
//
// Limited to only one regular texture (normal and lightmap might be added later).

IN vec2 uv;

uniform sampler2D texture;

void main() {

    FRAGCOLOR  = TEXTURE2D(texture, uv);
}
