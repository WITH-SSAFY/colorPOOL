<template>
  <div class="header wrap" :class="scrolled? 'scroll' : 'unscroll'">
    <v-row class="mx-16">
      <v-col cols="3"></v-col>
      <v-col cols="2" class="header-logo" v-if="isGet || isPick || !isLanding">
        <v-btn
          icon
          text
          class="ml-16"
          color="black"
          @click="goHome"
        >
          <!-- <img class="logo_img" src="../../assets/images/logo/logo_img.png"> -->
          <img class="logo_txt" src="../../assets/images/logo/logo_text.png">
        </v-btn>
      </v-col>
      <v-col cols="5" class="header-signin" v-if="!isLogin">
        <Signin></Signin>
      </v-col>
      <!-- <v-col style="text-align: center;" class="mt-2" v-if="isLogin">
      </v-col> -->

      <v-col cols="2" class="header-library" v-if="isLogin">
        <v-btn icon text @click="goLibrary">
          LIBRARY
        </v-btn>
      </v-col>
      <v-col cols="2" class="header-mypage" v-if="isLogin" @click="goMyPage">
        <v-btn icon text>
          MYPAGE
        </v-btn>
      </v-col>
      <v-col cols="2" class="header-logout" v-if="isLogin">
        <v-btn icon text @click="logout">
          LOGOUT
        </v-btn>
      </v-col>

    </v-row>
  </div>
</template>

<script>
import { mapGetters, mapActions } from 'vuex'
import Signin from './Signin'
const userStore = 'userStore'
const landingStore = 'landingStore'

export default {
  components: {
    Signin
  },
  computed: {
    ...mapGetters(userStore, { storeIsLogin: 'GE_IS_LOGIN',
                              storeUserInfo: 'GE_USER_INFO'}),
    ...mapGetters(landingStore, { storeIsLanding: 'GE_IS_LANDING', storeIsGet: 'GE_IS_GET', storeIsPick: 'GE_IS_PICK'}),
  },
  created(){
    this.userInfo = this.storeUserInfo;
    this.isLogin = this.storeIsLogin;
    this.isLanding = this.storeIsLanding;
    this.isGet = this.storeIsGet;
    this.isPick = this.storeIsPick;
  },
  data() {
    return {
      scrolled: false,
      userInfo: null,
      isLogin: false,
      isLanding: false,
      isPick: null,
      isGet: null,
    }
  },
  watch: {
    storeIsGet(val){
      this.isGet = val;
    }, 
    storeIsPick(val){
      this.isPick = val;
    },
    storeUserInfo(val){
      this.userInfo = val;
    },
    storeIsLogin(val){
      this.isLogin = val;
    },
    storeIsLanding(val){
      this.isLanding = val;
    }
  },
  methods: {
    ...mapActions(landingStore, ['AC_IS_GET','AC_IS_PICK', 'AC_IS_LANDING']),
    ...mapActions(userStore, ['AC_LOGOUT']),
    detectWindowScrollY() {
      // 감지 이벤트 메서드
      this.scrolled = window.scrollY > 250
    },
    goHome() {
      if(!this.isLanding){
        this.AC_IS_GET({isGet: false});
        this.AC_IS_PICK({isPick: false});
        this.$router.push({name: 'Landing'});
      } else {
        this.AC_IS_GET({isGet: false});
        this.AC_IS_PICK({isPick: false});
        console.log("isGet : ", this.isGet);
        console.log("isPick : ", this.isPick);
        window.location.reload();
        window.scrollTo({left: 0, top: 0, transition: 0});
      }
    },
    logout(){
      var result = confirm("정말 로그아웃 하시겠어요?");
      if(result){
          this.userInfo = null;
          this.AC_LOGOUT(null);
          localStorage.removeItem('access_token');
          localStorage.removeItem('kakao_token');
          localStorage.removeItem('google_token');
          if(!this.isLanding){
            this.AC_IS_GET({isGet: false});
            this.AC_IS_PICK({isPick: false});
            this.$router.push({name: 'Landing'});
            window.scrollTo(0, 0);
          } else {
            this.AC_IS_GET({isGet: false});
            this.AC_IS_PICK({isPick: false});
            window.scrollTo(0, 0);
            window.location.reload();
          }
      }
    },
    goMyPage(){
      this.$router.push({name: 'MyPage'});
      this.AC_IS_LANDING({isLanding: false});
    },
    goLibrary() {
      this.$router.push({name: 'Library'});
      this.AC_IS_LANDING({isLanding: false});
    }
  },
  mounted() {
    window.addEventListener('scroll', this.detectWindowScrollY)
  },
  beforeDestory() {
    window.removeEventListener('scroll', this.detectWindowScrollY)
  }
}
</script>

<style>
  .header.wrap {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 70px;
    padding: 0 1.5%;
    transition-duration: 300ms;
    z-index: 90;
    background-color: rgb(255, 255, 255, 0.8);
  }

  .header.wrap.scroll {
    background-color: rgb(255, 255, 255, 0.8);
  }

  .header.wrap.unscroll {
    background-color: rgba(238, 238, 238, 0.3);
  }

  .header.wrap table {
    width: 90%;
    margin-left: 5%;
    line-height: 3;
  }

  .header.wrap .header-logo {
    font-size: 25px;
    width: 60%;
    font-weight: bolder;
  }

  .header.wrap .header-logo .v-btn {
    margin-left: 5rem;
  }

  .header.wrap .header-logo .v-btn .logo_img {
    width: 3.5rem;
    height: 3.5rem;
  }

  .header.wrap .header-logo .v-btn .logo_txt {
    /* margin: 0 auto; */
    margin-top: 1rem;
    height: 2.7rem;
  }

  .header.wrap .header-library {
    text-align: right;
  }

  .header.wrap .header-library .v-btn{
    font-size: 1.0rem;
  }

  .header.wrap .header-signin {
    text-align: right;
  }
  
  .header.wrap .header-signin .v-btn{
    font-size: 1.0rem;
  }

  .header.wrap .header-signup {
    text-align: center;
  }

  .header.wrap .header-signup .v-btn{
    font-size: 1.0rem;
  }

  .header.wrap .header-mypage {
    text-align: center;
  }

  .header.wrap .header-mypage .v-btn{
    font-size: 1.0rem;
  }

  .header.wrap .header-logout {
    text-align: left;
  }

  .header.wrap .header-logout .v-btn{
    font-size: 1.0rem;
  }

</style>