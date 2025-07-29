declare const VERSION: string;
declare const SERVER_API_URL: string;
declare const DEVELOPMENT: string;
// Removed I18N_HASH declaration

declare module '*.json' {
  const value: any;
  export default value;
}
