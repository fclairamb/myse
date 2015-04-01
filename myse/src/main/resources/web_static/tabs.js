(function () {
	var app = angular.module('tabs', []);

	app.directive('myPanels', function () {
		return {
			restrict: 'E',
			templateUrl: 'panels.html',
			controller: function () {
				this.tab = 1;
				this.selectTab = function (tab) {
					this.tab = tab;
				};
				this.isSelected = function (checkTab) {
					//console.log('isSelected( ' + this.tab + ' / ' + checkTab + ' )');
					return this.tab === checkTab;
				};
			},
			controllerAs: 'pan'
		};
	});
})();
