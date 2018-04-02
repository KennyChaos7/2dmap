[TOC]
# 详细协议
https://note.youdao.com/share/?id=a9e434be060378db03ddeb4055049f8e&type=note#/


# 地图解析方法

### 1
    private List<FloatPoint> getFloatPointList(byte[] bytes,List<FloatPoint> floatPointList) {
            int x = 0;
            int y = 0;
            for (int i = 0; i < bytes.length; i++) {
                if (i > 0 && i % 25 == 0) {
                    y++;
                    x=0;
                }
                for (int j = 7; j > 0; j = j - 2) {
                    FloatPoint floatPoint = new FloatPoint(x, y);
                    String b1 = String.valueOf((byte) ((bytes[i] >> j) & 0x1));
                    String b2 = String.valueOf((byte) ((bytes[i] >> (j-1)) & 0x1));
                    int type = Integer.parseInt(b1+b2);
                    if (type!=0) {
                        floatPoint.type = type;
                        floatPointList.set(x+y*100,floatPoint);
                    }
                    x++;
                }
            }
            return floatPointList;
        }


### 2
    private int[][] analysis(int[][] data ,byte[] uncompress,int max)
        {
            for (int y = 0 ; y < max ; y++ )
                for (int x = 0 ; x < max ; x++ )
                {
                    data[y][x] = (( uncompress[(( y * max + x ) / 4 )] & 0xff ) >> ( ( 3 - x & 0x3) * 2 ) ) & 0x3 ;
                }

            return data;
        }
        
### 3 挖的坑

#### 1,地图导航线将会有时间上渐变
#### 2,像素和锯齿问题