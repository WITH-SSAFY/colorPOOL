<template>
  <div class="pick-color wrap" :class="[{active : this.$parent.isPick},{deactive : this.$parent.isGet}]">
    <div v-if="this.$parent.isLanding && this.$parent.isGet == false && this.$parent.isPick == false">
      <div class="underline"></div>
      <div class="pick-desc">
        <table>
          <tr>
            <td><span><strong>PICK</strong></span></td>
          </tr>
          <tr>
            <td><p>Pick your color from colorPOOL</p></td>
          </tr>
        </table>
      </div>
    </div>

    <!-- Landing page의 pickColor 화면 -->
    <v-card @click="clickPick()"
      class="mx-auto elevation-10"
      v-if="this.$parent.isPick == false && this.$parent.isGet == false"
    >
        <div style="overflow-x: hidden; height: 90%;">
          <img id="pick_img" src="../../../assets/images/colorimg.png">
          <!-- <p style="text-align: center; font-size: 1.1rem;">원하는 색에 대한 배색을 추천받아보세요</p> -->
        </div>
        <v-card-text style="position: absolute; bottom: 0; color: black; font-weight: 100; font-size: 18px; text-align: center; user-select: none;">
          원하는 색에 대한 배색을 추천받아보세요
        </v-card-text>

    </v-card>
    <!-- 컬러 팔레트 -->
    <ColorPalette v-if="this.$parent.isPick"></ColorPalette>

    <!-- 오른쪽 배경 -->
    <div v-if="this.$parent.isPick" class="pick-color right" v-bind:style="{'background-color' : backColor}">
      <!-- <img :src="imgUrl"> -->
      <img id="selected_img" :src="require(`@/assets/images/colorimages/${folder}/${pos}.jpg`)">
      <!-- 모달창 -->
      <v-dialog v-if="recommend" width="300" v-model="dialog">
        <template v-slot:activator="{ on, attrs }"> 
          <v-btn
            color="red lighten-2"
            dark
            v-bind="attrs"
            v-on="on"
          >
            Color Hint
          </v-btn>
        </template>
        <v-card width="300">
          <v-card-title class="headline grey lighten-2">Recommend Color</v-card-title>
          <v-banner :color="dummyColor.hex" height="300"></v-banner>
          <v-btn @click="goRecommend">Choose Recommended Color</v-btn>
        </v-card>
        
      </v-dialog>
      <div class="button-desc">
        <p>Get color recommendations</p>
        <p>based on the selected color</p>
      </div>
    </div>
    
    <!-- 배색 추천 화면 -->
    <div class="bottom-page" ref="messageDisplay">
      <RecommendTheme></RecommendTheme>
    </div>
  </div>
</template>

<script>
import axios from 'axios'
import { mapGetters, mapActions } from 'vuex'
import ColorPalette from './ColorPalette'
import RecommendTheme from '../recommend-theme/RecommendTheme'
import materialColors from '../../../assets/color/colorList.js'
const colorStore = 'colorStore'
const userStore = 'userStore'

export default {
  name: 'PickColor',
  components: {
    ColorPalette,
    RecommendTheme
  },
  created(){
    const bc = this.storeSelectedColor;
    if(bc === null){
      this.backColor = '';
    } else {
      console.log("bc", bc);
      this.backColor = bc.hex;
      this.folder = parseInt((this.storeSelectedColor.id) / 10) + 1;
      this.pos = (Number(this.storeSelectedColor.id) % 10);
    }
  },
  computed: {
    ...mapGetters(colorStore, {storeSelectedColor: 'GE_SELECTED_COLOR'}),
  },
  data () {
    return {
      materialColors : materialColors,
      selectedColor: '',
      selectedVariation: [],
      folder: '',
      pos: '',
      dialog: false,
      dummyPos: [Math.floor(Math.random() * 20), Math.floor(Math.random() * 10)],
      dummyColor: '',
      recommend: false,
    }
  },
  watch: {
    storeSelectedColor(val){
      this.selectedColor = val;
      this.$parent.selectedColor = val;
      this.backColor = val.hex;
      this.folder = parseInt((this.selectedColor.id) / 10) + 1;
      this.pos = (Number(this.selectedColor.id) % 10);
      const token = localStorage.getItem('access_token')
      const header = {
        'accept' : '*',
        'X-AUTH-TOKEN': token,
      }
      axios.get('https://cors-anywhere.herokuapp.com/https://j3a303.p.ssafy.io/api/recommend/color', {headers: header})
      .then((res) => {
        console.log(res)
        if(res.data.success == true) {
          this.recommend = true;
          this.dummyColor = this.materialColors[parseInt(res.data.data / 10)].variations[parseInt(res.data.data % 10)]
          console.log('get Color Recommendation',this.recommend);
        } else {
          this.recommend = false;
          console.log(this.recommend);
        }
      })
      .catch((err) => {
        console.log(err);
      })
    },
    storeIsLogin(val){
      this.isLogin = val;
    }
  },
  methods : {
    ...mapActions(colorStore, ['AC_SELECTED_COLOR', 'AC_THEMES']),
    ...mapActions(userStore, ['AC_DISPLAY']),
    clickPick() {
      this.$parent.isPick = true;
      this.$parent.isGet = false;
      const payload = { selectedColor: this.materialColors[0].variations[4]};
      this.AC_SELECTED_COLOR(payload);
    },
    goRecommend() {
      this.dialog = false;
      let color = this.dummyColor;
      const payload = {selectedColor : color};
      this.AC_SELECTED_COLOR(payload);
    }
  }
}
</script>

<style scoped>
  @font-face {
    font-family: 'ReenieBeanie-Regular';
    src: url('../../../assets/font/ReenieBeanie-Regular.ttf');
  }

  @font-face {
    font-family: 'Anton-Regular';
    src: url('../../../assets/font/Anton-Regular.ttf');
  }

  .pick-color.wrap {
    width: 50%;
    height: 100%;
    float: left;
    transition-duration: 300ms;
    display: flex;
    align-items: center;
  }

  .pick-color.wrap .underline{
    background-color: black;
    position: absolute;
    top: 30%;
    left: 5%;
    height: 0.7px;
    width: 40%;
  }

  .pick-color.wrap .pick-desc {
    text-align: right;
    position: absolute;
    top: 32%;
    right: 55%;
    font-size: 2.1rem;
    line-height: 0.7;
    width: 40%;
  }

  .pick-color.wrap .pick-desc span {
    float: left;
    font-size: 1.5rem;
  }

  .pick-color.wrap .pick-desc table {
    width: 100%;
    user-select: none;
  }

  .pick-color.wrap .pick-desc table tr:nth-child(1) {
    text-align: left;
    display: flex;
    align-items: center;
  }

  .pick-color.wrap .pick-desc table tr:nth-child(2) {
    text-align: right;
    font-size: 2rem;
  }

  .pick-color.wrap .pick-desc p {
    font-family: 'ReenieBeanie-Regular';
  }

  .pick-color.wrap.active {
    width: 100%;
    max-height: 100%;
  }

  .pick-color.wrap.deactive {
    width: 0%;
    height: 0%;
    opacity: 0;
  }

  .pick-color.wrap .v-card {
    position: absolute;
    left: 5%;
    width: 40%;
    height: 50%;
    margin-top: 17%;
  }

  .pick-color.wrap .v-card .v-image {
    height: 90%;
  }

  .pick-color.right {
    width: 30%;
    height: 100%;
    position: absolute;
    right: 0;
    display: flex;
    justify-content: center;
    align-items: center;
  }

  #selected_img {
    position: absolute;
    max-width: 90%;
    max-height: 80%;
    object-fit: contain;
    right: 50%;
  }


  #pick_img {
    height: 90%;
    transform-origin: left;
    transition-duration: 10s;
  }
  
  .pick-color.wrap .v-card:hover #pick_img {
    transform: translateX(calc(-100% + 550px));
  }

  .button-desc {
    position: absolute;
    font-size: 1.9rem;
    text-align: left;
    right: 20%;
    bottom: 15%;
    transform: rotate(-15deg);
    user-select: none;
  }
  
  .button-desc p {
    font-family: 'ReenieBeanie-Regular';
    line-height: 0.7;
  }

</style>